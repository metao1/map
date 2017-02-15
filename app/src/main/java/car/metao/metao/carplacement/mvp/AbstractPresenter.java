package car.metao.metao.carplacement.mvp;

public abstract class AbstractPresenter<V> {

    private V view;

    public V getView() {
        return view;
    }

    public void setView(V view) {
        this.view = view;
    }
}