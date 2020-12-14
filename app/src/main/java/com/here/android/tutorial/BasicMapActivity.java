/*
 * Copyright (c) 2011-2020 HERE Global B.V. and its affiliate(s).
 * All rights reserved.
 * The use of this software is conditional upon having a separate agreement
 * with a HERE company for the use or utilization of this software. In the
 * absence of such agreement, the use of the software is not allowed.
 */
package com.here.android.tutorial;

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;


import java.io.File;
import java.lang.Object;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.electronic_horizon.ElectronicHorizon;
import com.here.android.mpa.electronic_horizon.Link;
import com.here.android.mpa.electronic_horizon.PathTree;
import com.here.android.mpa.electronic_horizon.Position;
import com.here.android.mpa.guidance.TrafficUpdater;
import com.here.android.mpa.mapping.HistoricalTrafficRasterTileSource;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.TrafficEvent;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteElement;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.routing.RoutingZone;

import static android.util.Log.println;

public class BasicMapActivity extends FragmentActivity {

    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION };

    // map embedded in the map fragment
    private Map map = null;
    private MapRoute m_mapRoute;
    private boolean m_isExcludeRoutingZones;
    private AppCompatActivity m_activity;

    // map fragment embedded in this activity
    private AndroidXMapFragment mapFragment = null;
    private Object RouteElement;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();

    }


    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    //PositioningManager positioningManager = PositioningManager.getInstance();

    

    @SuppressWarnings("deprecation")
    private void initialize() {
        setContentView(R.layout.activity_main);



        // Search for the map fragment to finish setup by calling init().
        mapFragment = getMapFragment();

        // Use this method to specify custom map disk cache path.
        com.here.android.mpa.common.MapSettings.setDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps");

        // Initialize MapEngine
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    final OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Array containing string values of all available map schemes
                    List<String> schemes = map.getMapSchemes();
                    // Assume to select the 2nd map scheme in the available list
                    map.setMapScheme(Map.Scheme.NORMAL_DAY);

                    map.setTrafficInfoVisible(true);
                    String x = map.getMapTrafficLayer().getDisplayFilter().toString();
                    Log.i("traffico",x);

                    //TrafficUpdater.getInstance().getEvents(RouteElement, new TrafficUpdater.GetEventsListener() {
                    //    @Override
                    //    public void onComplete(@NonNull List<TrafficEvent> list, TrafficUpdater.Error error) {
                    //        for(TrafficEvent event : events){
                    //            if(event.isFlow()){
                    //                TrafficEvent.Severity severity = event.getSeverity();
                    //            }
                    //        }
                    //    }
                    //});
                    //HistoricalTrafficRasterTileSource tileSource =
                    //new HistoricalTrafficRasterTileSource(DaysOfTheWeek.WEDNESDAY.myvalue, 17, 40);
                    //map.addRasterTileSource(tileSource);
                    
                    // Set the map center to Bologna's center (no animation)
                    map.setCenter(new GeoCoordinate(44.493889, 11.342778, 0.0), Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);//PROVA CON GITHUB

                    createRoute(Collections.<RoutingZone>emptyList());


                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");



                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            new AlertDialog.Builder(BasicMapActivity.this).setMessage(
                                    "Error : " + error.name() + "\n\n" + error.getDetails())
                                    .setTitle(R.string.engine_init_error)
                                    .setNegativeButton(android.R.string.cancel,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    finishAffinity();
                                                }
                                            }).create().show();
                        }
                    });
                }
            }
        });
    }




    private void createRoute(final List<RoutingZone> excludedRoutingZones) {
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*TrafficUpdater.getInstance().getEvents(RouteElement, new TrafficUpdater.GetEventsListener() {
            @Override
            public void onComplete(@NonNull List<TrafficEvent> list, TrafficUpdater.Error error) {
                for(TrafficEvent event : events){
                    if(event.isFlow()){
                        TrafficEvent.Severity severity = event.getSeverity();
                    }
                }
            }
        });*/

        
        /*
         * Initialize a RouteOption. HERE Mobile SDK allow users to define their own parameters for the
         * route calculation,including transport modes,route types and route restrictions etc.Please
         * refer to API doc for full list of APIs
         */
        RouteOptions routeOptions = new RouteOptions();
        /* Other transport modes are also available e.g Pedestrian */
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        /* Disable highway in this route. */
        routeOptions.setHighwaysAllowed(false);
        /* Calculate the shortest route available. */
        routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);
        /* Exclude routing zones. */
        if (!excludedRoutingZones.isEmpty()) {
            routeOptions.excludeRoutingZones(toStringIds(excludedRoutingZones));
        }
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: Terracini */
        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(44.5146779122542, 11.319501399993898));
        /* END: Risorgimento */
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(44.48838896577573, 11.328245401382448));

        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);
        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>(){
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults, RoutingError routingError) {
                        /* Calculation is done. Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            Route route = routeResults.get(0).getRoute();

                            if (m_isExcludeRoutingZones && excludedRoutingZones.isEmpty()) {
                                // Here we exclude all available routing zones in the route.
                                // Also RoutingZoneRestrictionsChecker can be used to get
                                // available routing zones for specific RoadElement.
                                createRoute(route.getRoutingZones());
                            } else {
                                /* Create a MapRoute so that it can be placed on the map */
                                m_mapRoute = new MapRoute(route);

                                /* Show the maneuver number on top of the route */
                                m_mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                map.addMapObject(m_mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                map.zoomTo(route.getBoundingBox(), Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);
                            }
                        } else {
                            Toast.makeText(m_activity,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    static List<String> toStringIds(List<RoutingZone> excludedRoutingZones) {
        ArrayList<String> ids = new ArrayList<>();
        for (RoutingZone zone : excludedRoutingZones) {
            ids.add(zone.getId());
        }
        return ids;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    /**
     * Checks the dynamically controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }
}
