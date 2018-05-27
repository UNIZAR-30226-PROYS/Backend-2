package es.eina.sql.entities;

import es.eina.sql.utils.HibernateUtils;

import java.io.Serializable;

public abstract class EntityBase implements Serializable {

    protected int deleteEntity(){
        return HibernateUtils.deleteFromDB(this) ? 0 : 1;
    }
}
