package kr.co.photointerior.kosw.utils;

import com.blankj.utilcode.util.Utils;

import kr.co.photointerior.kosw.global.KoswApp;
import kr.co.photointerior.kosw.rest.DefaultRestClient;
import kr.co.photointerior.kosw.rest.api.App;
import kr.co.photointerior.kosw.rest.api.CafeService;
import kr.co.photointerior.kosw.rest.api.CustomerService;
import kr.co.photointerior.kosw.rest.api.UserService;

public interface SingletoneMixin {
    default KoswApp getApp() {
        return (KoswApp) Utils.getApp();
    }

    default CafeService getCafeService() {
        final KoswApp app = getApp();
        if (app.cafeService == null) {
            app.cafeService = new DefaultRestClient<CafeService>(app.getApplicationContext())
                    .getClient(CafeService.class);
        }
        return app.cafeService;
    }

    default App getAppService() {
        final KoswApp app = getApp();
        if (app.appService == null) {
            app.appService = new DefaultRestClient<App>(app.getApplicationContext())
                    .getClient(App.class);
        }
        return app.appService;
    }

    default UserService getUserService() {
        final KoswApp app = getApp();
        if (app.userService == null) {
            app.userService = new DefaultRestClient<UserService>(app.getApplicationContext())
                    .getClient(UserService.class);
        }
        return app.userService;
    }

    default CustomerService getCustomerService() {
        final KoswApp app = getApp();
        if (app.customerService == null) {
            app.customerService = new DefaultRestClient<CustomerService>(app.getApplicationContext())
                    .getClient(CustomerService.class);
        }
        return app.customerService;
    }
}
