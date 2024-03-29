package org.mian.gitnex.helpers.ssl;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import androidx.core.app.NotificationCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;

/**
 * @author Georg Lukas, modified by opyale
 */
@SuppressLint("CustomX509TrustManager")
public class MemorizingTrustManager implements X509TrustManager {

	public static final String KEYSTORE_NAME = "keystore";
	public static final String KEYSTORE_KEY = "keystore";
	private static final int NOTIFICATION_ID = 100509;
	private final SparseArray<MTMDecision> openDecisions = new SparseArray<>();
	private final X509TrustManager defaultTrustManager;
	private Context context;
	private NotificationManager notificationManager;
	private int decisionId = 0;
	private SharedPreferences keyStoreStorage;
	private KeyStore appKeyStore;
	private X509TrustManager appTrustManager;

	/**
	 * Creates an instance of the MemorizingTrustManager class that falls back to a custom
	 * TrustManager.
	 *
	 * <p>You need to supply the application context. This has to be one of: - Application -
	 * Activity - Service
	 *
	 * <p>The context is used for file management, to display the dialog / notification and for
	 * obtaining translated strings.
	 *
	 * @param m Context for the application.
	 * @param defaultTrustManager Delegate trust management to this TM. If null, the user must
	 *     accept every certificate.
	 */
	public MemorizingTrustManager(Context m, X509TrustManager defaultTrustManager) {

		init(m);
		this.appTrustManager = getTrustManager(appKeyStore);
		this.defaultTrustManager = defaultTrustManager;
	}

	/**
	 * Creates an instance of the MemorizingTrustManager class using the system X509TrustManager.
	 *
	 * <p>You need to supply the application context. This has to be one of: - Application -
	 * Activity - Service
	 *
	 * <p>The context is used for file management, to display the dialog / notification and for
	 * obtaining translated strings.
	 *
	 * @param m Context for the application.
	 */
	public MemorizingTrustManager(Context m) {

		init(m);
		this.appTrustManager = getTrustManager(appKeyStore);
		this.defaultTrustManager = getTrustManager(null);
	}

	/**
	 * Returns a X509TrustManager list containing a new instance of TrustManagerFactory.
	 *
	 * <p>This function is meant for convenience only. You can use it as follows to integrate
	 * TrustManagerFactory for HTTPS sockets:
	 *
	 * <pre>
	 *     SSLContext sc = SSLContext.getInstance("TLS");
	 *     sc.init(null, MemorizingTrustManager.getInstanceList(this),
	 *         new java.security.SecureRandom());
	 *     HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	 * </pre>
	 *
	 * @param c Activity or Service to show the Dialog / Notification
	 */
	public static X509TrustManager[] getInstanceList(Context c) {

		return new X509TrustManager[] {new MemorizingTrustManager(c)};
	}

	private static boolean isExpiredException(Throwable e) {

		do {
			if (e instanceof CertificateExpiredException) {
				return true;
			}
			e = e.getCause();
		} while (e != null);

		return false;
	}

	private static boolean isPathException(Throwable e) {

		do {
			if (e instanceof CertPathValidatorException) {
				return true;
			}

			e = e.getCause();
		} while (e != null);

		return false;
	}

	private static String hexString(byte[] data) {

		StringBuilder si = new StringBuilder();

		for (int i = 0; i < data.length; i++) {
			si.append(String.format("%02x", data[i]));
			if (i < data.length - 1) {
				si.append(":");
			}
		}

		return si.toString();
	}

	private static String certHash(final X509Certificate cert, String digest) {

		try {
			MessageDigest md = MessageDigest.getInstance(digest);
			md.update(cert.getEncoded());
			return hexString(md.digest());
		} catch (CertificateEncodingException | NoSuchAlgorithmException e) {
			return e.getMessage();
		}
	}

	/**
	 * Reflectively call {@code Notification.setLatestEventInfo(Context, CharSequence, CharSequence,
	 * PendingIntent)} since it was remove in Android API level 23.
	 */
	private static void setLatestEventInfoReflective(
			Notification notification,
			Context context,
			CharSequence mtmNotification,
			CharSequence certName,
			PendingIntent call) {

		Method setLatestEventInfo;

		try {
			setLatestEventInfo =
					notification
							.getClass()
							.getMethod(
									"setLatestEventInfo",
									Context.class,
									CharSequence.class,
									CharSequence.class,
									PendingIntent.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}

		try {
			setLatestEventInfo.invoke(notification, context, mtmNotification, certName, call);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	private void init(Context m) {

		context = m;
		notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		keyStoreStorage = m.getSharedPreferences(KEYSTORE_NAME, Context.MODE_PRIVATE);
		appKeyStore = loadAppKeyStore();
	}

	/**
	 * Get a list of all certificate aliases stored in MTM.
	 *
	 * @return an {@link Enumeration} of all certificates
	 */
	private Enumeration<String> getCertificates() {

		try {
			return appKeyStore.aliases();
		} catch (KeyStoreException e) {
			// this should never happen, however...
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a certificate for a given alias.
	 *
	 * @param alias the certificate's alias as returned by {@link #getCertificates()}.
	 * @return the certificate associated with the alias or <tt>null</tt> if none found.
	 */
	public Certificate getCertificate(String alias) {

		try {
			return appKeyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			// this should never happen, however...
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes the given certificate from MTMs key store.
	 *
	 * <p><b>WARNING</b>: this does not immediately invalidate the certificate. It is well possible
	 * that (a) data is transmitted over still existing connections or (b) new connections are
	 * created using TLS renegotiation, without a new cert check.
	 *
	 * @param alias the certificate's alias as returned by {@link #getCertificates()}.
	 * @throws KeyStoreException if the certificate could not be deleted.
	 */
	public void deleteCertificate(String alias) throws KeyStoreException {

		appKeyStore.deleteEntry(alias);
		keyStoreUpdated();
	}

	/**
	 * Creates a new hostname verifier supporting user interaction.
	 *
	 * <p>This method creates a new {@link HostnameVerifier} that is bound to the given instance of
	 * {@link MemorizingTrustManager}, and leverages an existing {@link HostnameVerifier}. The
	 * returned verifier performs the following steps, returning as soon as one of them succeeds:
	 * /p>
	 *
	 * <ol>
	 *   <li>Success, if the wrapped defaultVerifier accepts the certificate.
	 *   <li>Success, if the server certificate is stored in the keystore under the given hostname.
	 *   <li>Ask the user and return accordingly.
	 *   <li>Failure on exception.
	 * </ol>
	 *
	 * @param defaultVerifier the {@link HostnameVerifier} that should perform the actual check
	 * @return a new hostname verifier using the MTM's key store
	 * @throws IllegalArgumentException if the defaultVerifier parameter is null
	 */
	public HostnameVerifier wrapHostnameVerifier(final HostnameVerifier defaultVerifier) {

		if (defaultVerifier == null) {
			throw new IllegalArgumentException("The default verifier may not be null");
		}

		return new MemorizingHostnameVerifier(defaultVerifier);
	}

	private X509TrustManager getTrustManager(KeyStore ks) {

		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(ks);
			for (TrustManager t : tmf.getTrustManagers()) {
				if (t instanceof X509TrustManager) {
					return (X509TrustManager) t;
				}
			}
		} catch (Exception e) {
			Log.e("MemorizingTrustManager", Objects.requireNonNull(e.getMessage()));
		}

		return null;
	}

	private KeyStore loadAppKeyStore() {

		KeyStore keyStore;

		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			return null;
		}

		try {
			keyStore.load(null, null);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			e.getMessage();
		}

		String keystore = keyStoreStorage.getString(KEYSTORE_KEY, null);

		if (keystore != null) {
			ByteArrayInputStream inputStream =
					new ByteArrayInputStream(Base64.decode(keystore, Base64.DEFAULT));

			try {
				keyStore.load(inputStream, "MTM".toCharArray());
				inputStream.close();
			} catch (Exception e) {
				Log.e("MemorizingTrustManager", Objects.requireNonNull(e.getMessage()));
			}
		}

		return keyStore;
	}

	private void storeCert(String alias, Certificate cert) {

		try {
			appKeyStore.setCertificateEntry(alias, cert);
		} catch (KeyStoreException e) {
			return;
		}

		keyStoreUpdated();
	}

	private void storeCert(X509Certificate cert) {

		storeCert(cert.getSubjectDN().toString(), cert);
	}

	private void keyStoreUpdated() {
		// reload appTrustManager
		appTrustManager = getTrustManager(appKeyStore);

		// store KeyStore to shared preferences
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			appKeyStore.store(byteArrayOutputStream, "MTM".toCharArray());
			byteArrayOutputStream.flush();
			byteArrayOutputStream.close();

			keyStoreStorage
					.edit()
					.putString(
							KEYSTORE_KEY,
							Base64.encodeToString(
									byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
					.apply();
		} catch (Exception e) {
			Log.e("MemorizingTrustManager", Objects.requireNonNull(e.getMessage()));
		}
	}

	// if the certificate is stored in the app key store, it is considered "known"
	private boolean isCertKnown(X509Certificate cert) {

		try {
			return appKeyStore.getCertificateAlias(cert) != null;
		} catch (KeyStoreException e) {
			return false;
		}
	}

	private void checkCertTrusted(X509Certificate[] chain, String authType, boolean isServer)
			throws CertificateException {
		try {

			if (isServer) {
				appTrustManager.checkServerTrusted(chain, authType);
			} else {
				appTrustManager.checkClientTrusted(chain, authType);
			}
		} catch (CertificateException ae) {
			// if the cert is stored in our appTrustManager, we ignore expired ones
			if (isExpiredException(ae) || isCertKnown(chain[0])) {
				return;
			}

			try {
				if (defaultTrustManager == null) {
					throw ae;
				}
				if (isServer) {
					defaultTrustManager.checkServerTrusted(chain, authType);
				} else {
					defaultTrustManager.checkClientTrusted(chain, authType);
				}
			} catch (CertificateException e) {
				interactCert(chain, authType, e);
			}
		}
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {

		checkCertTrusted(chain, authType, false);
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {

		checkCertTrusted(chain, authType, true);
	}

	public X509Certificate[] getAcceptedIssuers() {

		return defaultTrustManager.getAcceptedIssuers();
	}

	private int createDecisionId(MTMDecision d) {

		int myId;
		synchronized (openDecisions) {
			myId = decisionId;
			openDecisions.put(myId, d);
			decisionId += 1;
		}
		return myId;
	}

	private void certDetails(StringBuilder stringBuilder, X509Certificate c) {

		SimpleDateFormat validityDateFormatter =
				new SimpleDateFormat(
						"yyyy-MM-dd", context.getResources().getConfiguration().locale);

		stringBuilder
				.append("\n")
				.append(c.getSubjectDN().toString())
				.append("\n")
				.append(validityDateFormatter.format(c.getNotBefore()))
				.append(" - ")
				.append(validityDateFormatter.format(c.getNotAfter()))
				.append("\nSHA-256: ")
				.append(certHash(c, "SHA-256"))
				.append("\nSHA-1: ")
				.append(certHash(c, "SHA-1"))
				.append("\nSigned by: ")
				.append(c.getIssuerDN().toString())
				.append("\n");
	}

	private String certChainMessage(final X509Certificate[] chain, CertificateException cause) {

		Throwable e = cause;
		StringBuilder stringBuilder = new StringBuilder();

		if (isPathException(e)) {
			stringBuilder.append(context.getString(R.string.mtmTrustAnchor));
		} else if (isExpiredException(e)) {
			stringBuilder.append(context.getString(R.string.mtmCertExpired));
		} else {
			// get to the cause
			while (e.getCause() != null) {
				e = e.getCause();
			}

			stringBuilder.append(e.getLocalizedMessage());
		}

		stringBuilder.append("\n\n");
		stringBuilder.append(context.getString(R.string.mtmConnectAnyway));
		stringBuilder.append("\n\n");
		stringBuilder.append(context.getString(R.string.mtmCertDetails));

		for (X509Certificate c : chain) {
			certDetails(stringBuilder, c);
		}

		return stringBuilder.toString();
	}

	private String hostNameMessage(X509Certificate cert, String hostname) {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(context.getString(R.string.mtmHostnameMismatch, hostname));
		stringBuilder.append("\n\n");

		try {
			Collection<List<?>> sans = cert.getSubjectAlternativeNames();

			if (sans == null) {
				stringBuilder.append(cert.getSubjectDN());
				stringBuilder.append("\n");
			} else {
				for (List<?> altName : sans) {
					Object name = altName.get(1);
					if (name instanceof String) {
						stringBuilder.append("[");
						stringBuilder.append(altName.get(0));
						stringBuilder.append("] ");
						stringBuilder.append(name);
						stringBuilder.append("\n");
					}
				}
			}
		} catch (CertificateParsingException e) {
			Log.e("MemorizingTrustManager", Objects.requireNonNull(e.getMessage()));
			stringBuilder.append("<Parsing error: ");
			stringBuilder.append(e.getLocalizedMessage());
			stringBuilder.append(">\n");
		}

		stringBuilder.append("\n");
		stringBuilder.append(context.getString(R.string.mtmConnectAnyway));
		stringBuilder.append("\n\n");
		stringBuilder.append(context.getString(R.string.mtmCertDetails));
		certDetails(stringBuilder, cert);
		return stringBuilder.toString();
	}

	private void startActivityNotification(Intent intent, int decisionId, String certName) {

		final PendingIntent call = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE);
		final String mtmNotification = context.getString(R.string.mtmNotification);

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context, "ssl")
						.setSmallIcon(android.R.drawable.ic_lock_lock)
						.setContentTitle(mtmNotification)
						.setContentText(certName)
						.setTicker(certName)
						.setContentIntent(call)
						.setAutoCancel(true)
						.setPriority(NotificationCompat.PRIORITY_HIGH);

		notificationManager.notify(NOTIFICATION_ID + decisionId, builder.build());
	}

	private int interact(final String message, final int titleId) {
		MTMDecision choice = new MTMDecision();
		final int myId = createDecisionId(choice);

		((BaseActivity) context)
				.runOnUiThread(
						() -> {
							MaterialAlertDialogBuilder materialAlertDialogBuilder =
									new MaterialAlertDialogBuilder(context)
											.setTitle(titleId)
											.setMessage(message)
											.setPositiveButton(
													R.string.mtmDecisionAlways,
													(dialog, which) ->
															interactResult(
																	myId,
																	MTMDecision.DECISION_ALWAYS))
											.setNeutralButton(
													R.string.mtmDecisionAbort,
													(dialog, which) ->
															interactResult(
																	myId,
																	MTMDecision.DECISION_ABORT))
											.setOnCancelListener(
													dialog ->
															interactResult(
																	myId,
																	MTMDecision.DECISION_ABORT));

							materialAlertDialogBuilder.create().show();
						});

		try {
			synchronized (choice) {
				choice.wait();
			}
		} catch (InterruptedException e) {
			Log.e("MemorizingTrustManager", Objects.requireNonNull(e.getMessage()));
		}

		return choice.state;
	}

	private void interactCert(
			final X509Certificate[] chain, String authType, CertificateException cause)
			throws CertificateException {

		if (interact(certChainMessage(chain, cause), R.string.mtmAcceptCert)
				== MTMDecision.DECISION_ALWAYS) {
			storeCert(chain[0]); // only store the server cert, not the whole chain
		} else {
			throw (cause);
		}
	}

	private boolean interactHostname(X509Certificate cert, String hostname) {

		if (interact(hostNameMessage(cert, hostname), R.string.mtmAcceptServerName)
				== MTMDecision.DECISION_ALWAYS) {
			storeCert(hostname, cert);
			return true;
		}

		return false;
	}

	private void interactResult(int decisionId, int choice) {

		MTMDecision d;

		synchronized (openDecisions) {
			d = openDecisions.get(decisionId);
			openDecisions.remove(decisionId);
		}

		if (d == null) {
			return;
		}

		synchronized (d) {
			d.state = choice;
			d.notify();
		}
	}

	class MemorizingHostnameVerifier implements HostnameVerifier {

		private final HostnameVerifier defaultVerifier;

		MemorizingHostnameVerifier(HostnameVerifier wrapped) {

			defaultVerifier = wrapped;
		}

		@Override
		public boolean verify(String hostname, SSLSession session) {
			// if the default verifier accepts the hostname, we are done
			if (defaultVerifier.verify(hostname, session)) {
				return true;
			}

			// otherwise, we check if the hostname is an alias for this cert in our keystore
			try {
				X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];

				if (cert.equals(appKeyStore.getCertificate(hostname.toLowerCase(Locale.US)))) {
					return true;
				} else {
					return interactHostname(cert, hostname);
				}
			} catch (Exception e) {
				return false;
			}
		}
	}
}
