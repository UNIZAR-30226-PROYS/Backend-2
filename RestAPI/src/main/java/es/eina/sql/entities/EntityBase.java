package es.eina.sql.entities;

import org.json.JSONObject;

public abstract class EntityBase {
    public static final long CACHE_INVALIDATE_TIME = 1800000L;

    private long invalidateTime;
    private boolean dirty;

    public final boolean isDirty(){
        return dirty;
    }

    protected final void update(){
        update(CACHE_INVALIDATE_TIME);
    }

    protected final void update(long validTime){
        invalidateTime = System.currentTimeMillis() + validTime;
        dirty = true;
    }

    public final boolean isEntityValid(){
        return isEntityValid(System.currentTimeMillis());
    }

    public final boolean isEntityValid(long time){
        return time <= invalidateTime;
    }

}
