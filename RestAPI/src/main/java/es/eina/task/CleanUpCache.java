package es.eina.task;

import es.eina.RestApp;
import es.eina.cache.TokenManager;
import es.eina.cache.UserCache;
import es.eina.cache.UserIdCache;
import es.eina.sql.MySQLQueries;
import es.eina.sql.parameters.SQLParameterLong;

public class CleanUpCache extends TaskBase {

    public CleanUpCache() {
        super(900000, false);
    }

    /**
     * Runs a batch of clean through all the caches.
     */
    @Override
    public void run() {
        RestApp.getInstance().getLogger().info("Performing cache clean up.");
        long time = System.currentTimeMillis();
        TokenManager.cleanUp(time);
        UserIdCache.cleanUp(time);
        UserCache.cleanUp(time);

        RestApp.getSql().runAsyncUpdate(MySQLQueries.DELETE_EXPIRED_TOKENS, new SQLParameterLong(time));
    }
}
