package car.metao.metao.carplacement;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import car.metao.metao.carplacement.model.PlaceMark;
import com.metao.async.repository.Repository;
import com.metao.async.repository.RepositoryCallback;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String API_END_POINT = "http://192.168.1.3/website/v5/drivers.php";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Repository<PlaceMark> placeMarkRepository;
        placeMarkRepository = new Repository<PlaceMark>("PlaceMarkRepository") {
            static final long RAM_SIZE = 40 * 1024 * 1024;// 40MiB Repo Size

            @Override
            public long getRamUsedInBytes() {
                return RAM_SIZE;
            }

            @Override
            public RepositoryType repositoryType() {
                return RepositoryType.JSON;
            }
        };
        placeMarkRepository.addService(API_END_POINT, new RepositoryCallback<PlaceMark>() {
            @Override
            public void onDownloadFinished(String urlAddress, PlaceMark placeMark) {
                Log.d("tag", String.valueOf(placeMark.placemarks.size()));
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d("tag", String.valueOf(throwable.toString()));
            }
        });
    }
}
