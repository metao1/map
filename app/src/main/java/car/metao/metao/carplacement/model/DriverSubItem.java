package car.metao.metao.carplacement.model;

import java.io.Serializable;

/**
 * Created by metao on 2/14/2017.
 */
public class DriverSubItem implements Serializable {
    private String itemOne;
    private String itemTwo;

    public DriverSubItem(String itemOne, String itemTwo) {
        this.itemOne = itemOne;
        this.itemTwo = itemTwo;
    }

    public String getItemOne() {
        return itemOne;
    }

    public void setItemOne(String itemOne) {
        this.itemOne = itemOne;
    }

    public String getItemTwo() {
        return itemTwo;
    }

    public void setItemTwo(String itemTwo) {
        this.itemTwo = itemTwo;
    }
}
