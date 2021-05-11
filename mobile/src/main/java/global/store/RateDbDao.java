package global.store;

import global.AgenorRate;

/**
 * Created by furszy on 3/3/18.
 */

public interface RateDbDao<T> extends AbstractDbDao<T>{

    AgenorRate getRate(String coin);


    void insertOrUpdateIfExist(AgenorRate agenorRate);

}
