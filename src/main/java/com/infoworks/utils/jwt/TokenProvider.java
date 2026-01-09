package com.infoworks.utils.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.MessageParser;
import com.infoworks.utils.jwt.models.JWTHeader;
import com.infoworks.utils.jwt.models.JWTPayload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface TokenProvider extends AutoCloseable{

	Key generateKey(String...args);
	String generateToken(String secret, JWTPayload payload, Calendar timeToLive) throws RuntimeException;
	String refreshToken(String token, Calendar timeToLive) throws RuntimeException;
	void makeExpire() throws RuntimeException;
	void dispose();

	Boolean isValid(String token, String...args);
	default String getIssuer(String token) { return TokenProvider.getPayloadValue("iss", token); }
	default String getUserID(String token) { return TokenProvider.getPayloadValue("iss", token); }
	default String getSubject(String token) { return TokenProvider.getPayloadValue("sub", token); }
	default String getIat(String token) { return TokenProvider.getPayloadValue("iat", token); }
	default String getExp(String token) { return TokenProvider.getPayloadValue("exp", token); }

	/**
	 * By Default time to live is 60*60 sec(1 hour), if not provided.
	 * @param duration
	 * @return
	 */
	static Calendar timeToLive(Duration duration, TimeUnit unit) {
		unit = (unit == null) ? TimeUnit.SECONDS : unit;
		int amount = (duration == null) ? (60 * 60) : Long.valueOf(duration.toSeconds()).intValue();
        Calendar cal = Calendar.getInstance();
		if (unit == TimeUnit.MILLISECONDS) {
			amount = Long.valueOf(duration.toMillis()).intValue();
			cal.add(Calendar.MILLISECOND, amount);
		} else if (unit == TimeUnit.MINUTES) {
			amount = Long.valueOf(duration.toMinutes()).intValue();
			cal.add(Calendar.MINUTE, amount);
		} else if (unit == TimeUnit.HOURS) {
			amount = Long.valueOf(duration.toHours()).intValue();
			cal.add(Calendar.HOUR, amount);
		} else if (unit == TimeUnit.DAYS) {
			amount = Long.valueOf(duration.toDays()).intValue();
			cal.add(Calendar.DAY_OF_YEAR, amount);
		} else {
			cal.add(Calendar.SECOND, amount);
		}
        return cal;
    }

	static  <Header extends JWTHeader> Header parseHeader(String token, Class<Header> payloadClass) throws RuntimeException {
		if (token == null || token.isEmpty()) return null;
		String[] parts = token.split("\\.");
		if (parts.length > 1) {
			try {
				ObjectMapper mapper = MessageParser.getJsonSerializer();
				Header header = mapper.readValue(new String(Base64.getDecoder().decode(parts[0])), payloadClass);
				return header;
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	static  <Payload extends JWTPayload> Payload parsePayload(String token, Class<Payload> payloadClass) throws RuntimeException {
		String decodedValue = parsePayload(token);
		if (decodedValue != null){
			try {
				ObjectMapper mapper = MessageParser.getJsonSerializer();
				Payload payload = mapper.readValue(decodedValue, payloadClass);
				return payload;
			} catch (IOException e) {throw new RuntimeException(e);}
		}
		return null;
	}

	static String parsePayload(String token) {
		if (token == null || token.isEmpty()) return null;
		String[] sections = token.split("\\.");
		if (sections.length > 2) {
			String payload64 = sections[1];
			byte[] decoded = Base64.getDecoder().decode(payload64);
			String decodedValue = new String(decoded);
			if (decodedValue != null && decodedValue.startsWith("{")) {
				return decodedValue;
			}
		}
		return null;
	}

	static String getPayloadValue(String key, String token) throws RuntimeException {
		if (key == null || key.isEmpty()) return null;
		String decodedValue = parsePayload(token);
		if (decodedValue != null){
			try {
				ObjectMapper mapper = MessageParser.getJsonSerializer();
				Map<String, String> payload = mapper.readValue(decodedValue
						, new TypeReference<Map<String, String>>(){});
				return (payload != null) ? payload.get(key) : null;
			} catch (IOException e) {throw new RuntimeException(e);}
		}
		return null;
	}

	static String parseToken(String token, String prefix) {
		if (token.trim().startsWith(prefix)) {
			String pToken = token.trim();
			return pToken.substring(prefix.length());
		} else {
			return token;
		}
	}

	static String encode(String strs) {
		return encode(strs.getBytes(StandardCharsets.UTF_8));
	}

	static String encode(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	static String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}
}
