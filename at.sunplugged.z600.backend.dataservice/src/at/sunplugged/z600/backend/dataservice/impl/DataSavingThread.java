package at.sunplugged.z600.backend.dataservice.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osgi.service.log.LogService;

import at.sunplugged.z600.backend.dataservice.api.DataServiceException;
import at.sunplugged.z600.common.settings.api.NetworkComIds;
import at.sunplugged.z600.common.settings.api.SettingsService;

public class DataSavingThread extends Thread {

	private static DataSavingThread instance = null;

	private static String API_URL;

	private static String API_NEXT_DATAPOINT;

	private static String API_NEW_SESSION;

	public static String API_POST_DATAPOINT;

	private HttpClient client;

	public static void startInstance() throws DataServiceException {
		if (instance == null) {
			instance = new DataSavingThread();
			instance.client = HttpClientBuilder.create().build();
			instance.start();
		} else {
			throw new DataServiceException("There is already a running instance of the DataSavingThread");
		}
	}

	public static void stopInstance() {
		if (instance != null) {
			instance.setRunning(false);

		}
	}

	private volatile boolean running = false;

	private DataSavingThread() {
		this.setName("Data Saving Thread");
		this.setDaemon(true);

		setUrlSettings();

	}

	private void setUrlSettings() {
		SettingsService settings = DataServiceImpl.getSettingsServce();

		API_URL = settings.getProperty(NetworkComIds.HTTP_BASE_SERVER_URL) + "sessions/api";
		API_NEXT_DATAPOINT = API_URL + "/nextdatapoint/";
		API_NEW_SESSION = API_URL + "/newSession";
		API_POST_DATAPOINT = API_URL + "/putdatapoint";
	}

	@Override
	public void run() {
		boolean restart = true;
		
		while(restart) {
			restart = false;
			running = true;
			long timestep = Long
					.valueOf(DataServiceImpl.getSettingsServce().getProperty(NetworkComIds.SQL_UPDATE_TIME_STEP));

			int exceptionCount = 0;
			int sessionId;
			int dataPoint;

			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpGet sessionGet = new HttpGet(API_NEW_SESSION);

			try {

				if (HttpHelper.checkIfHttpServerIsRunning(client) == false) {
					DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Failed to connect to http server.");
					setRunning(false);
					return;
				}
				CloseableHttpResponse response = client.execute(sessionGet);
				try {
					BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					sessionId = Integer.valueOf(bf.readLine());
				} finally {
					response.close();
				}

				dataPoint = getNextDataPoint(client, sessionId);

				while (isRunning()) {
					Thread.sleep(timestep);

					if (WriteDataTableUtils.writeHttpDataTable(client, sessionId, dataPoint) == false) {
						LogService log = DataServiceImpl.getLogService();
						log.log(LogService.LOG_DEBUG, "Recovering from Connecting error to http server. SessionId: "
								+ sessionId + " - DataPoint: " + dataPoint);
						timestep *= 10;
						log.log(LogService.LOG_DEBUG, "Reducing logspeed to " + timestep);

						while (isRunning()) {
							log.log(LogService.LOG_DEBUG, "Trying to reconnect to http server.");

							if (HttpHelper.checkIfHttpServerIsRunning(client)) {

								log.log(LogService.LOG_INFO, "Reconnected to http server.");
								log.log(LogService.LOG_DEBUG, "Setting logspeed to previous value.");
								timestep *= 0.1;

								dataPoint = getNextDataPoint(client, sessionId);

								log.log(LogService.LOG_DEBUG,
										"Continue logging at session: " + sessionId + " - DataPoint: " + dataPoint);
								break;
							}
							Thread.sleep(timestep);
						}
					} else {
						dataPoint++;
					}

				}

			} catch (ClientProtocolException e2) {
				DataServiceImpl.getLogService().log(LogService.LOG_ERROR,
						"DataSavingThread failed because of IO. Restarting!", e2);
				restart = restartDataSavingThread();
			} catch (IOException e2) {
				DataServiceImpl.getLogService().log(LogService.LOG_ERROR,
						"DataSavingThread failed because of IO. Restarting in 5s...", e2);
				restart = restartDataSavingThread();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			running = false;	
		}
		

	}

	private boolean restartDataSavingThread() {
		if (running == true) {
			try {
				DataServiceImpl.getLogService().log(LogService.LOG_DEBUG, "Restarting DatasavingThread in 5s...");
				Thread.sleep(5000);
				DataServiceImpl.getLogService().log(LogService.LOG_DEBUG, "Datasavingthread restarted!");
				return true;
			} catch (InterruptedException e) {
				DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Restarting of DataSavingThread interrupted!",
						e);
				return false;
			}
		}
		return false;

	}

	private int getNextDataPoint(CloseableHttpClient client, int sessionId)
			throws UnsupportedOperationException, IOException {
		HttpGet dataPointGet = new HttpGet(API_NEXT_DATAPOINT + sessionId);

		CloseableHttpResponse response2 = client.execute(dataPointGet);
		try {
			BufferedReader bf2 = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));

			return Integer.valueOf(bf2.readLine());
		} finally {
			response2.close();
		}

	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {

		if (running == false) {

			DataServiceImpl.getLogService().log(LogService.LOG_DEBUG, "DataService Thread stopped.");
			try {
				if (instance != null) {
					((CloseableHttpClient) instance.client).close();
				}
			} catch (IOException e) {
				DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Failed to close http client.", e);
			}
			instance = null;
		}

		this.running = running;
	}
}
