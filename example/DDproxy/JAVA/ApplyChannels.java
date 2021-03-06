 import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.UnsupportedEncodingException;
    import java.security.NoSuchAlgorithmException;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.concurrent.CountDownLatch;
    import java.util.concurrent.Future;

    import org.apache.commons.codec.binary.Hex;
    import org.apache.commons.codec.digest.DigestUtils;
    import org.apache.http.HttpResponse;
    import org.apache.http.client.config.RequestConfig;
    import org.apache.http.client.methods.HttpPost;
    import org.apache.http.concurrent.FutureCallback;
    import org.apache.http.entity.StringEntity;
    import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
    import org.apache.http.impl.nio.client.HttpAsyncClients;

    import com.alibaba.fastjson.JSON;
    import com.zx.util.StringUtil;

    /**
     * 申请独享动态示例
     * @author Administrator
     *
     */
    public class ApplyChannels {
    	//
    	private static String secret = "a048184e6e7c4ea1a8e43e13c5f35b38";//个人中心获取
    	private static String timestamp = StringUtil.substring(String.valueOf(System.currentTimeMillis()), 0, 10);
    	private static String url = "http://api.xdaili.cn/xdaili-api/spider/applyChannels";
    	private static String spiderId = "2d39e4737e16492d9f792227308728e2";//个人中心获取

    	/**
    	 * 生成token规则
    	 *
    	 * @param timestamp 时间戳(取前10位)
    	 * @param map(body的所有参数)
    	 * @return
    	 * @throws NoSuchAlgorithmException
    	 */
    	private static String getToken(String timestamp, Map<String, Object> map) throws NoSuchAlgorithmException {
    		// 根据map的key值排序
    		String[] keyArray = map.keySet().toArray(new String[0]);
    		Arrays.sort(keyArray);
    		// 排序后,key+value进行拼接
    		StringBuilder paramStr = new StringBuilder();
    		for (String key : keyArray) {
    			paramStr.append(key).append(map.get(key));
    		}
    		// 生成sign
    		String sign = new String(Hex.encodeHex(DigestUtils.sha(paramStr.toString()))).toUpperCase();
    		// 生成token
    		String token = org.apache.commons.codec.digest.DigestUtils.md5Hex(timestamp + secret + sign);
    		return token;
    	}

    	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("count", 2);
    		HttpPost post = new HttpPost(url);
    		String token = ApplyChannels.getToken(timestamp, map);
    		// post请求传过去的头部参数，body参数
    		post.addHeader("token", token);
    		post.addHeader("spiderId", spiderId);
    		post.addHeader("timestamp", timestamp);
    		post.addHeader("Content-Type", "application/json");
    		// body参数json格式
    		post.setEntity(new StringEntity(JSON.toJSONString(map)));
    		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(150000).setConnectTimeout(150000).build();
    		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
    		try {
    			httpclient.start();
    			final CountDownLatch latch = new CountDownLatch(1);
    			final StringBuilder result = new StringBuilder();
    			// post请求
    			Future<HttpResponse> future = httpclient.execute(post, new FutureCallback<HttpResponse>() {
    				@Override
    				public void completed(final HttpResponse response) {
    					BufferedReader reader = null;
    					try {
    						reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    						String line = null;
    						while ((line = reader.readLine()) != null) {
    							result.append(line + "\n");
    						}
    						System.out.println(result);
    					} catch (IOException e) {
    					} finally {
    						try {
    							reader.close();
    						} catch (IOException e) {
    						}
    					}
    					latch.countDown();
    				}

    				@Override
    				public void failed(final Exception ex) {
    					latch.countDown();
    				}

    				@Override
    				public void cancelled() {
    					latch.countDown();
    				}

    			});
    			latch.await();
    		} catch (Exception e) {
    		} finally {
    			try {
    				httpclient.close();
    			} catch (IOException e) {

    			}
    		}

    	}
    }
                   
