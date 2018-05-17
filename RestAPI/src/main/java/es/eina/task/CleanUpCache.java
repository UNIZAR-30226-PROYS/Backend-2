package es.eina.task;

import es.eina.RestApp;
import es.eina.cache.UserCache;

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
        UserCache.cleanUp(time);

        //RestApp.getSql().runAsyncUpdate(MySQLQueries.DELETE_EXPIRED_TOKENS, new SQLParameterLong(time));
    }
}
