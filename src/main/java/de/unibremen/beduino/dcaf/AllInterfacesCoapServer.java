/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Kai Hudalla (Bosch Software Innovations GmbH) - add endpoints for all IP addresses
 ******************************************************************************/
package de.unibremen.beduino.dcaf;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class AllInterfacesCoapServer extends org.eclipse.californium.core.CoapServer {

	private static Logger logger = LoggerFactory.getLogger(AllInterfacesCoapServer.class);

	/**
	 * Creates a new AllInterfacesCoapServer which listens on all available IP addresses.
	 */
	public AllInterfacesCoapServer() {
		addEndpoints();
		start();

		String interfaces = getEndpoints().stream().
				map(Endpoint::getAddress).
				map(a -> "[" + a.getHostString() + ":" + a.getPort() + "]")
				.collect(Collectors.joining(", "));


		logger.info("DCAF server running on " + interfaces + ".");
	}


	/**
	 * Add individual endpoints listening on default CoAP port on all ddresses of all network interfaces.
	 */
	private void addEndpoints() {
		int coapPort = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

		for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
			InetSocketAddress bindToAddress = new InetSocketAddress(addr, coapPort);
			addEndpoint(new CoapEndpoint(bindToAddress));
		}
	}


}
