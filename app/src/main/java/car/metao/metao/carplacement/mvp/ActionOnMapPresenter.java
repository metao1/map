package car.metao.metao.carplacement.mvp;

/**
 * Created by metao on 2/14/2017.
 */
public class ActionOnMapPresenter extends AbstractPresenter<MapListener> {

    private MapListener mapListener;

    public MapListener getPresenter() {
        return mapListener;
    }

    @Override
    public void setView(MapListener view) {
        mapListener = view;
    }
}
