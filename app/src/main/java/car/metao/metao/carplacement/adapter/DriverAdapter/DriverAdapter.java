package car.metao.metao.carplacement.adapter.DriverAdapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import car.metao.metao.carplacement.R;
import car.metao.metao.carplacement.model.DriverSubItem;

import java.util.ArrayList;

/**
 * Created by metao on 2/14/2017.
 */
public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverPlaceHolder> {
    private ArrayList<DriverSubItem> driverSubItems;
    private String TAG = DriverAdapter.class.getSimpleName();

    public DriverAdapter(ArrayList<DriverSubItem> driverSubItems) {
        this.driverSubItems = driverSubItems;
    }

    @Override
    public DriverPlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflate = layoutInflater.inflate(R.layout.driver_item, parent, false);
        return new DriverPlaceHolder(inflate);
    }

    @Override
    public void onBindViewHolder(DriverPlaceHolder holder, int position) {
        DriverSubItem driverSubItem = driverSubItems.get(position);
        if (driverSubItem != null) {
            holder.bindDriver(driverSubItem);
        }
    }

    @Override
    public int getItemCount() {
        return (driverSubItems != null) ? driverSubItems.size() : 0;
    }

    class DriverPlaceHolder extends RecyclerView.ViewHolder {

        private final TextView firstTextView, secondTextView;

        DriverPlaceHolder(View itemView) {
            super(itemView);
            firstTextView = (TextView) itemView.findViewById(R.id.firstText);
            secondTextView = (TextView) itemView.findViewById(R.id.secondText);
        }

        void bindDriver(DriverSubItem driverSubItem) {
            firstTextView.setText(driverSubItem.getItemOne());
            secondTextView.setText(driverSubItem.getItemTwo());
        }
    }
}
