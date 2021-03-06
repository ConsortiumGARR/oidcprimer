package oidc_rp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.ResponseType.Value;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformationResponse;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationRequest;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientRegistrationResponseParser;
import com.nimbusds.openid.connect.sdk.util.DefaultJWTDecoder;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Session;

public class Client {
	// TODO specify the correct URL
	public static String ISSUER = "https://mitreid.org/";

	public static String flow = "code";

	private JsonObject jsonClientMetadata = null;

	private OIDCClientInformation clientInformation;
	private OIDCProviderMetadata providerMetadata;

	public Client(String clientMetadataString)
			throws ParseException, URISyntaxException, IOException, SerializeException {

		flow = "code";

		// TODO obtain provider configuration information
		obtainProviderInformation();

		JsonReader jsonReader = Json.createReader(new StringReader(clientMetadataString));

		jsonClientMetadata = jsonReader.readObject();

		OIDCClientMetadata clientMetadata = OIDCClientMetadata.parse(JSONObjectUtils.parse(clientMetadataString));

		/*
		 * PLEASE NOTE: client_id and client_secret are null at first, cause you
		 * didn't already registered your client. That is ok, since it will
		 * trigger dynamic registration. Once registered, you'll get client_id
		 * and client_secret in the success page. Insert them in the
		 * clients.json file in the following key/value pairs:
		 *
		 * [..] "client_id": null, "client_secret": null [..]
		 *
		 * Failing to do that will cause the client to register again each time
		 * you authenticate.
		 * 
		 */

		JsonValue jsonClientID = jsonClientMetadata.get("client_id");
		JsonValue jsonClientSecret = jsonClientMetadata.get("client_secret");

		if (jsonClientID.equals(JsonValue.NULL) && jsonClientSecret.equals(JsonValue.NULL)) {
			registerClient(clientMetadata);
		} else {
		    ClientID clientID = new ClientID(((JsonString) jsonClientID).getString());
		    Secret clientSecret = new Secret(((JsonString) jsonClientSecret).getString());

			clientInformation = new OIDCClientInformation(clientID, new Date(), clientMetadata, clientSecret);
		}
	}

	private void obtainProviderInformation()
			throws URISyntaxException, MalformedURLException, IOException, ParseException {
		// TODO obtain provide information and save them in providerMetadata
	}

	private void registerClient(OIDCClientMetadata clientMetadata)
			throws IOException, SerializeException, ParseException {
		// TODO Make registration request

		// TODO Parse and check response

		// TODO Store client information from OP in clientInformation
	}

	private ResponseType getResponeTypeForAuth() {
		if ("code".equals(flow)) {
			return new ResponseType(ResponseType.Value.CODE);
		} else if ("implicit".equals(flow)) {
			ResponseType rt = new ResponseType(ResponseType.Value.TOKEN);
			rt.add(new Value("id_token"));
			return rt;
		} else {
			return null;
		}
	}

	private URI getRedirectUriForAuth() throws URISyntaxException {
		if ("code".equals(flow)) {
			return URI.create(jsonClientMetadata.getJsonArray("redirect_uris").getString(0));
		} else if ("implicit".equals(flow)) {
			return URI.create(jsonClientMetadata.getJsonArray("redirect_uris").getString(1));
		} else {
			return null;
		}
	}

	public String authenticate(Request req, Response res) throws URISyntaxException, SerializeException {
		// session object that can be used to store state between requests
		Session session = req.session();

		// TODO make authentication request
		// Generate random state string for pairing the response to the request
		session.attribute("state", new State());
		// Generate nonce
		session.attribute("nonce", new Nonce());
		// Specify scope
		Scope scope = Scope.parse(Arrays.asList("openid", "profile", "email"));

		// Compose the request
		AuthenticationRequest authenticationRequest = new AuthenticationRequest(
				providerMetadata.getAuthorizationEndpointURI(), getResponeTypeForAuth(), scope,
				clientInformation.getID(), getRedirectUriForAuth(), (State) session.attribute("state"),
				(Nonce) session.attribute("nonce"));

		// TODO insert the redirect URL
		String login_url = authenticationRequest.toURI().toString();
		res.redirect(login_url);
		return null;
	}

	public String codeFlowCallback(Request req, Response res) throws IOException {
		Session session = req.session();

		// Callback redirect URI
		String url = req.url() + "?" + req.raw().getQueryString();

		// TODO parse authentication response from url

		// TODO make token request

		// TODO validate the ID Token according to the OpenID Connect spec (sec
		// 3.1.3.7.)

		// TODO make userinfo request

		// TODO set the appropriate values
		String clientID = null;
		String clientSecret = null;
		AuthorizationCode authCode = null;
		AccessToken accessToken = null;
		String parsedIdToken = null;
		ReadOnlyJWTClaimsSet idTokenClaims = null;
		UserInfoSuccessResponse successUIResponse = null;

		return WebServer.successPage(clientID, clientSecret, authCode, accessToken, parsedIdToken, idTokenClaims,
				successUIResponse);
	}

	public String implicitFlowCallback(Request req, Response res) throws IOException {
		Session session = req.session();

		// Callback redirect URI
		String url = req.url() + "#" + req.queryParams("url_fragment");

		// TODO parse authentication response from url

		// TODO validate the ID Token according to the OpenID Connect spec (sec
		// 3.2.2.11.)

		// TODO set the appropriate values

		// TODO make userinfo request

		// TODO set the appropriate values
		String clientID = null;
		String clientSecret = null;
		AuthorizationCode authCode = null;
		AccessToken accessToken = null;
		String parsedIdToken = null;
		ReadOnlyJWTClaimsSet idTokenClaims = null;
		UserInfoSuccessResponse successUIResponse = null;

		return WebServer.successPage(clientID, clientSecret, authCode, accessToken, parsedIdToken, idTokenClaims,
				successUIResponse);
	}

	private ReadOnlyJWTClaimsSet verifyIdToken(JWT idToken, OIDCProviderMetadata providerMetadata) {
		RSAPublicKey providerKey = null;
		try {
			JSONObject key = getProviderRSAJWK(providerMetadata.getJWKSetURI().toURL().openStream());
			if (key != null)
				providerKey = RSAKey.parse(key).toRSAPublicKey();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | java.text.ParseException
				| ParseException e) {
			// TODO error handling
			return null;
		}

		DefaultJWTDecoder jwtDecoder = new DefaultJWTDecoder();
		if (providerKey != null)
			jwtDecoder.addJWSVerifier(new RSASSAVerifier(providerKey));
		ReadOnlyJWTClaimsSet claims = null;
		try {
			claims = jwtDecoder.decodeJWT(idToken);
		} catch (JOSEException | java.text.ParseException e) {
			// TODO error handling
			return null;
		}

		return claims;
	}

	private JSONObject getProviderRSAJWK(InputStream is) throws ParseException {
		// Read all data from stream
		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(is);) {
			while (scanner.hasNext()) {
				sb.append(scanner.next());
			}
		}

		// Parse the data as json
		String jsonString = sb.toString();
		JSONObject json = JSONObjectUtils.parse(jsonString);

		// Find the RSA signing key
		JSONArray keyList = (JSONArray) json.get("keys");
		for (Object key : keyList) {
			JSONObject k = (JSONObject) key;
			if ((k.get("use") == null || "sig".equals(k.get("use"))) && "RSA".equals(k.get("kty"))) {
				return k;
			}
		}
		return null;
	}
}
