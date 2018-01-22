package org.kcervan.masterlistas;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListasActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private FlowingDrawer mDrawer;

    private FirebaseAnalytics analytics;
    private FirebaseRemoteConfig remoteConfig;
    private static final int CACHE_TIME_SECONDS = 30;
    private AdView adView;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd ad;
    private IInAppBillingService serviceBilling;
    private ServiceConnection serviceConnection;
    private final String ID_ARTICULO = "org.kcervan.masterlistas.producto";
    private final int INAPP_BILLING = 1;
    private final String developerPayLoad = "clave de seguridad";
    private final String ID_SUSCRIPCION = "org.kcervan.masterlistas.suscripcion";
    private boolean showInterticial = false;
    com.facebook.ads.AdView adViewFace;
    private NativeAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listas);
        showCrossPromoDialog();
        analytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.presiono_fab, Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        //Inicializar los elementos
        List items = new ArrayList();
        items.add(new Lista(R.drawable.trabajo, getString(R.string.trabajo), 2));
        items.add(new Lista(R.drawable.casa, getString(R.string.personal), 3));
        // Obtener el Recycler
        recycler = (RecyclerView) findViewById(R.id.reciclador);
        recycler.setHasFixedSize(true);
        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);
        // Crear un nuevo adaptador
        adapter = new ListaAdapter(items);
        recycler.setAdapter(adapter);

        recycler.addOnItemTouchListener(
                new RecyclerItemClickListener(ListasActivity.this,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                Intent intent = new Intent(ListasActivity.this,
                                        DetalleListaActivity.class);
                                intent.putExtra("numeroLista", position);
                                ActivityOptionsCompat options = ActivityOptionsCompat.
                                        makeSceneTransitionAnimation(ListasActivity.this,
                                                new Pair<View, String>(v.findViewById(R.id.imagen),
                                                        getString(R.string.transition_name_img)),
                                                new Pair<View, String>(findViewById(R.id.fab),
                                                        getString(R.string.transition_fab))
                                        );
                                ActivityCompat.startActivity(ListasActivity.this, intent, options
                                        .toBundle());
                            }
                        })
        );

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Navigation Drawer
        NavigationView navigationView = (NavigationView) findViewById(
                R.id.vNavigation);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_compartir:
                                compatirTexto("http://play.google.com/store/apps/details?id="
                                        + getPackageName());
                                break;
                            case R.id.nav_compartir_lista:
                                compatirTexto("LISTA DE LA COMPRA: patatas, leche, huevos. ---- " +
                                        "Compartido por: http://play.google.com/store/apps/details?id=" +
                                        getPackageName());
                                break;
                            case R.id.nav_compartir_logo:
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                                        R.drawable.logo);
                                compatirBitmap(bitmap, "Compartido por: " +
                                        "http://play.google.com/store/apps/details?id=" + getPackageName());
                                break;
                            case R.id.nav_compartir_desarrollador:
                                compatirTexto("http://play.google.com/store/apps/dev?id=7027410910970713274");
                                break;
                            case R.id.nav_1:
                                if (ad.isLoaded()) {
                                    ad.show();
                                }
                                break;
                            case R.id.nav_articulo_no_recurrente:
                                comprarProducto();
                                break;
                            case R.id.nav_consulta_inapps_disponibles:
                                getInAppInformationOfProducts();
                                break;
                            case R.id.nav_susbripcion:
                                comprarSuscripcion(ListasActivity.this);
                                break;
                            case R.id.nav_consulta_subs_disponibles:
                                getSubscriptionInformationOfProducts();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), menuItem.getTitle(),
                                        Toast.LENGTH_SHORT).show();

                        }
                        return false;
                    }
                });
        mDrawer = (FlowingDrawer) findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.toggleMenu();
                Bundle param = new Bundle();
                param.putString("elemento", "boton menu");
                param.putInt("pulsacion_ms", 34);
                analytics.logEvent("Menu", param);
            }
        });

        Transition lista_enter = TransitionInflater.from(this)
                .inflateTransition(R.transition.transition_lista_enter);
        getWindow().setEnterTransition(lista_enter);


        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings config = new FirebaseRemoteConfigSettings
                .Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        remoteConfig.setConfigSettings(config);
        remoteConfig.setDefaults(R.xml.remote_config);
        remoteConfig.fetch(CACHE_TIME_SECONDS)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ListasActivity.this, R.string.fetch_ok,
                                    Toast.LENGTH_SHORT).show();
                            remoteConfig.activateFetched();
                        } else {
                            Toast.makeText(ListasActivity.this, R.string.fetch_fallado,
                                    Toast.LENGTH_SHORT).show();
                        }
                        boolean open = remoteConfig.getBoolean("navigation_drawer_abierto");
                        if (open) {
                            abrePrimeraVez();
                            analytics.setUserProperty("experimento_menu", "true");
                        } else {
                            analytics.setUserProperty("experimento_menu", "false");
                        }
                    }
                });

        new RateMyApp(this).app_launched();
        adView = (AdView) findViewById(R.id.adView);

        FloatingActionButton f = (FloatingActionButton) findViewById(R.id.fab);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (interstitialAd != null) {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        Toast.makeText(ListasActivity.this,
                                "El Anuncio no esta disponible aun", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        ad = MobileAds.getRewardedVideoAdInstance(this);
        ad.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Toast.makeText(ListasActivity.this, "Vídeo Bonificado cargado",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
                ad.loadAd("ca-app-pub-9780467258849720/7176889933", new AdRequest
                        .Builder().addTestDevice("3097747E039701F659335E42352CEB71").build());
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Toast.makeText(ListasActivity.this, "onRewarded: moneda virtual: " +
                                rewardItem.getType() + " aumento: " + rewardItem.getAmount(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
            }
        });
        ad.loadAd("ca-app-pub-9780467258849720/7176889933", new AdRequest.Builder().
                addTestDevice("3097747E039701F659335E42352CEB71").build());

        serviceConectInAppBilling();

        AdSettings.addTestDevice("3097747E039701F659335E42352CEB71");

        crearAnuncioBannerFacebook();

        crearAnuncioIntersticialFacebook();

        crearAnuncioNativoFacebook();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isMenuVisible()) {
            mDrawer.closeMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (adViewFace != null) {
            adViewFace.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INAPP_BILLING: {
                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData =
                        data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra(
                        "INAPP_DATA_SIGNATURE");
                if (resultCode == RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        String developerPayload = jo.getString("developerPayload");
                        String purchaseToken = jo.getString("purchaseToken");
                        if (sku.equals(ID_ARTICULO)) {
                            Toast.makeText(this, "Compra completada",
                                    Toast.LENGTH_LONG).show();
                            setAds(false);
                            backToBuy(purchaseToken);
                        } else if (sku.equals(ID_SUSCRIPCION)) {
                            Toast.makeText(this, "Suscrición correcta", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void abrePrimeraVez() {
        SharedPreferences sp = getSharedPreferences("mispreferencias", 0);
        boolean primerAcceso = sp.getBoolean("abrePrimeraVez", true);
        if (primerAcceso) {
            mDrawer.openMenu();
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("abrePrimeraVez", false).commit();
        }
    }

    void compatirTexto(String texto) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(i, "Selecciona aplicación"));
    }

    void compatirBitmap(Bitmap bitmap, String texto) {
// guardamos bitmap en el directorio cache
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream s = new FileOutputStream(cachePath + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, s);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
// Obtenemos la URI usando el FileProvider
        File path = new File(getCacheDir(), "images");
        File file = new File(path, "image.png");
        Uri uri = FileProvider.getUriForFile(this, "org.kcervan.masterlistas.fileprovider", file);
//Compartimos la URI
        if (uri != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
// temp permission for receiving app to read this file
            i.setDataAndType(uri, getContentResolver().getType(uri));
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.putExtra(Intent.EXTRA_TEXT, texto);
            startActivity(Intent.createChooser(i, "Selecciona aplicación"));
        }
    }

    private void showCrossPromoDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_AppCompat);
        dialog.setContentView(R.layout.dialog_crosspromotion);
        dialog.setCancelable(true);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button boton = (Button) dialog.findViewById(R.id.buttonDescargar);
        boton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?" +
                                "id=com.mimisoftware.emojicreatoremoticonosemoticones")));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void serviceConectInAppBilling() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBilling = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceBilling = IInAppBillingService.Stub.asInterface(service);
                checkPurchasedInAppProducts();
                checkPurchasedSubscriptions();
            }
        };
        Intent serviceIntent = new Intent(
                "com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void comprarProducto() {
        if (serviceBilling != null) {
            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = serviceBilling.getBuyIntent(3, getPackageName(),
                        ID_ARTICULO, "inapp", developerPayLoad);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            PendingIntent pendingIntent = buyIntentBundle
                    .getParcelable("BUY_INTENT");
            try {
                if (pendingIntent != null) {
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            INAPP_BILLING, new Intent(), 0, 0, 0);
                }
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "InApp Billing service not available",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void backToBuy(String token) {
        if (serviceBilling != null) {
            try {
                int response = serviceBilling.consumePurchase(
                        3, getPackageName(), token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void getInAppInformationOfProducts() {
        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add(ID_ARTICULO);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        Bundle skuDetails;
        ArrayList<String> responseList;
        try {
            skuDetails = serviceBilling.getSkuDetails(3, getPackageName(),
                    "inapp", querySkus);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                assert responseList != null;
                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String ref = object.getString("productId");
                    System.out.println("InApp Reference: " + ref);
                    String price = object.getString("price");
                    System.out.println("InApp Price: " + price);
                }
            }
        } catch (RemoteException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkPurchasedInAppProducts() {
        Bundle ownedItemsInApp = null;
        if (serviceBilling != null) {
            try {
                ownedItemsInApp = serviceBilling.getPurchases(3, getPackageName(), "inapp", null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            int response = ownedItemsInApp.getInt("RESPONSE_CODE");
            System.out.println(response);
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItemsInApp
                        .getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItemsInApp
                        .getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItemsInApp
                        .getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken = ownedItemsInApp
                        .getString("INAPP_CONTINUATION_TOKEN");
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);
                    System.out.println("Inapp Purchase data: " +
                            purchaseData);
                    System.out.println("Inapp Signature: " + signature);
                    System.out.println("Inapp Sku: " + sku);
                    if (sku.equals(ID_ARTICULO)) {
                        Toast.makeText(this,
                                "Inapp comprado: " + sku + "el dia " +
                                        purchaseData, Toast.LENGTH_LONG).show();
                        setAds(false);
                    } else {
                        setAds(true);
                    }
                }
            }
        }
    }

    public void comprarSuscripcion(Activity activity) {

        if (serviceBilling != null) {
            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = serviceBilling.getBuyIntent(3, activity
                        .getPackageName(), ID_SUSCRIPCION, "subs", developerPayLoad);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            assert buyIntentBundle != null;
            PendingIntent pendingIntent =
                    buyIntentBundle.getParcelable("BUY_INTENT");
            try {
                assert pendingIntent != null;
                activity.startIntentSenderForResult(pendingIntent
                        .getIntentSender(), INAPP_BILLING, new Intent(), 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(activity, "Servicio de suscripción no disponible",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getSubscriptionInformationOfProducts() {
        ArrayList<String> skuListSubs = new ArrayList<String>();
        skuListSubs.add(ID_SUSCRIPCION);
        Bundle querySkusSubs = new Bundle();
        querySkusSubs.putStringArrayList("ITEM_ID_LIST", skuListSubs);
        Bundle skuDetailsSubs;
        ArrayList<String> responseListSubs;
        try {
            skuDetailsSubs = serviceBilling.getSkuDetails(
                    3, getPackageName(), "subs", querySkusSubs);
            int responseSubs = skuDetailsSubs.getInt("RESPONSE_CODE");
            System.out.println(responseSubs);
            if (responseSubs == 0) {
                responseListSubs = skuDetailsSubs.getStringArrayList(
                        "DETAILS_LIST");
                assert responseListSubs != null;
                for (String thisResponse : responseListSubs) {
                    JSONObject object = new JSONObject(thisResponse);
                    String ref = object.getString("productId");
                    System.out.println("Subscription Reference: " + ref);
                    String price = object.getString("price");
                    System.out.println("Subscription Price: " + price);
                }
            }
        } catch (RemoteException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkPurchasedSubscriptions() {
        Bundle ownedItemsInApp = null;
        if (serviceBilling != null) {
            try {
                ownedItemsInApp = serviceBilling.getPurchases(
                        3, getPackageName(), "subs", null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            int response = ownedItemsInApp.getInt("RESPONSE_CODE");
            System.out.println(response);
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItemsInApp
                        .getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItemsInApp
                        .getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItemsInApp
                        .getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken = ownedItemsInApp
                        .getString("INAPP_CONTINUATION_TOKEN");
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);
                    System.out.println("Suscription Purchase data: " +
                            purchaseData);
                    System.out.println("Suscription Signature: " + signature);
                    System.out.println("Suscription Sku: " + sku);
                    if (sku.equals(ID_SUSCRIPCION)) {
                        Toast.makeText(this, "Suscrito correctamente: " + sku +
                                "el dia " + purchaseData, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void setAds(Boolean adsEnabled) {
        if (adsEnabled) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("88609E7D422D2F6ED44D85C0E9962BCA").build();
            adView.loadAd(adRequest);
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId("ca-app-pub-9780467258849720/3500410949");
            interstitialAd.loadAd(new AdRequest.Builder()
                    .addTestDevice("88609E7D422D2F6ED44D85C0E9962BCA").build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(new AdRequest.Builder()
                            .addTestDevice("88609E7D422D2F6ED44D85C0E9962BCA").build());
                }
            });
            showInterticial = true;
        } else {
            showInterticial = false;
            adView.setVisibility(View.GONE);
        }
    }


    private void crearAnuncioBannerFacebook() {
        adViewFace = new com.facebook.ads.AdView(this, "1748944961790994_1748961771789313", AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adViewFace);
        adViewFace.loadAd();
    }

    private void crearAnuncioIntersticialFacebook() {

        final com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(this,
                "1748944961790994_1748987335120090");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Toast.makeText(ListasActivity.this, "Error: " +
                        adError.getErrorMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        });
        interstitialAd.loadAd();
    }

    private void crearAnuncioNativoFacebook() {

        nativeAd = new NativeAd(this, "1748944961790994_1749011211784369");
        nativeAd.setAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }
                LinearLayout nativeAdContainer = (LinearLayout)
                        findViewById(R.id.native_ad_container);
                LayoutInflater inflater = LayoutInflater.from(
                        ListasActivity.this);
                LinearLayout adView = (LinearLayout) inflater.inflate(
                        R.layout.native_ad, nativeAdContainer, false);
                nativeAdContainer.addView(adView);
                ImageView nativeAdIcon = (ImageView) adView.findViewById(
                        R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView) adView.findViewById(
                        R.id.native_ad_title);
                MediaView nativeAdMedia = (MediaView) adView.findViewById(
                        R.id.native_ad_media);
                TextView nativeAdSocialContext = (TextView) adView.findViewById(
                        R.id.native_ad_social_context);
                TextView nativeAdBody = (TextView) adView.findViewById(
                        R.id.native_ad_body);
                Button nativeAdCallToAction = (Button) adView.findViewById(
                        R.id.native_ad_call_to_action);
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                nativeAdBody.setText(nativeAd.getAdBody());
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);
                nativeAdMedia.setNativeAd(nativeAd);
                LinearLayout adChoicesContainer = (LinearLayout) findViewById(
                        R.id.ad_choices_container);
                AdChoicesView adChoicesView = new AdChoicesView(
                        ListasActivity.this, nativeAd, true);
                adChoicesContainer.addView(adChoicesView);
                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdCallToAction);
                nativeAd.registerViewForInteraction(nativeAdContainer,
                        clickableViews);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        });
        nativeAd.loadAd();
    }
}
