package de.se.SAP;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.*;

// From https://gist.github.com/justasm/8f86eab6108183db93ac
public class NonPersistentCookieJar implements CookieJar {
    private final Set<Cookie> cookieStore = new LinkedHashSet<>();

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.addAll(cookies);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> matchingCookies = new ArrayList<>();
        Iterator<Cookie> it = cookieStore.iterator();
        while (it.hasNext()) {
            Cookie cookie = it.next();
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                it.remove();
            } else if (cookie.matches(url)) {
                matchingCookies.add(cookie);
            }
        }
        return matchingCookies;
    }
}
