package car.metao.metao.carplacement.mvp;

/**
 * Created by metao on 2/14/2017.
 */
public class ResultOnMapPresenter extends AbstractPresenter<MapUpdateListener> {

    private MapUpdateListener mapListener;

    public MapUpdateListener getPresenter() {
        return mapListener;
    }

    @Override
    public void setView(MapUpdateListener view) {
        mapListener = view;
    }
}
