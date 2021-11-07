package kr.co.photointerior.kosw.ui.fragment;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.ResourceUtils;

import kr.co.photointerior.kosw.R;

/**
 * 도움말 1페이지
 */
public class FragmentHelpOne extends BaseFragment {
    private WebView mWebView;

    public static BaseFragment newInstance() {
        return new FragmentHelpOne();
    }

    @Override
    protected void followingWorksAfterInflateView() {
        mWebView = getView(R.id.web_view);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        // mWebView.loadUrl(Env.getHtmlUrl(Env.UrlPath.STAIR_CODE));
        String html = ResourceUtils.readAssets2String("info.html");
        mWebView.loadDataWithBaseURL(
                "file:///android_asset/",
                html,
                "text/html",
                "utf-8",
                null
        );
    }


    @Override
    public int getViewResourceId() {
        return R.layout.fragment_help_one_new;
    }
}
