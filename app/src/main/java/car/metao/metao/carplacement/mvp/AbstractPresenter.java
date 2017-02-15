package car.metao.metao.carplacement.mvp;

/**
 * Created by metao on 1/16/2017.
 */
public abstract class AbstractPresenter<V>{

    private V view;

    public AbstractPresenter() {
    }

    public abstract void onCreate();

    public V getPresenter() {
        return view;
    }

    public void setView(V view) {
        this.view = view;
    }
}