package com.knziha.plod.widgets;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.knziha.plod.plaindict.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Enables TLS v1.2 when creating SSLSockets.
 * <p/>
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * default only from API 20.
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
public class Tls12SocketFactory extends SSLSocketFactory {
	public static OkHttpClient.Builder enableTls12OnPreLollipop(Context context, OkHttpClient.Builder client) {
		//HTTPSCerUtils.setTrustAllCertificate(client);
//		if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
//			try {
//				SSLContext sc = SSLContext.getInstance("TLSv1.2");
//				sc.init(null, null, null);
//				client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
//
//				ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//						.tlsVersions(TlsVersion.TLS_1_2).build();
//
//				List<ConnectionSpec> specs = new ArrayList<>();
//				specs.add(cs);
//				specs.add(ConnectionSpec.COMPATIBLE_TLS);
//				specs.add(ConnectionSpec.CLEARTEXT);
//
//				client.connectionSpecs(specs);
//			} catch (Exception exc) {
//				Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
//			}
//		}
		
		return client;
	}

	
	
	private static final String[] TLS_V12_ONLY = {"TLSv1.2"};

    final SSLSocketFactory delegate;

    public Tls12SocketFactory(SSLSocketFactory base) {
        this.delegate = base;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return patch(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return patch(delegate.createSocket(address, port, localAddress, localPort));
    }

    private Socket patch(Socket s) {
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(TLS_V12_ONLY);
        }
        return s;
    }
}