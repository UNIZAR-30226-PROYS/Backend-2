package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
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

    protected int deleteEntity(){
        Transaction tr = null;
        try (Session session = HibernateUtils.getSessionFactory().openSession()) {
            tr = session.beginTransaction();
            session.delete(this);
            tr.commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            return -1;
        }
    }

}
