package com.pipis.ceidproject;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends Activity implements OnMapLongClickListener, OnMenuItemClickListener, RoutingListener
{
	// Variables
	private GoogleMap mMap;
	
	// fb variable
	private UiLifecycleHelper uiHelper;
	
	// logic variables
	private boolean addMark = false;
	private boolean update = false;
	private boolean bool = false;
	private boolean bool2 = false;
	
	// dialog box
	private AlertDialog.Builder dialogBuilder;
	
	// database handler
	private MyDBHandler dbHandler;
	
	// user input
	private String input;
		
	// hashMap
	private Map<Marker, String> markerArray = new HashMap<Marker, String>();

	// strings to hold input
	private String title;
	private String description;
	private String category;
	private String startPointTitle;
	private String endPointTitle;
	private String titleToDelete;
	
	// textview menu
	private TextView mtv;
		
	// coordinates between 2 points
	private LatLng endPointPosition, startPointPosition;
	
	// GPS
	private GPSTracker gps;
	
	// directions type
	private String directionsType;
	private String way_of_direction;
	
	

	/**
	 * Initialization of our activity elements
	 */
	protected void onCreate(Bundle savedInstanceState)
	{ 
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	     
	    // initialize objects
	    dbHandler = new MyDBHandler(this, null, null, 1);
	    mtv = (TextView) findViewById(R.id.textView1);    
	    uiHelper = new UiLifecycleHelper(this, null);
	    gps = new GPSTracker(MainActivity.this);
	    
	    // initialize google map
	    setUpMapIfNeeded();  
	       
	    // Marker click listener, shows markers menu
    	mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
    	{	
			public void onInfoWindowClick(Marker marker)
			{						
				// identify which Marker we want to delete by it's title
				titleToDelete = marker.getTitle();
				
				if (bool)
				{
					bool = false;
					endPointPosition = marker.getPosition();
					findDirections();		
				}
				else
				{
					startPointPosition = marker.getPosition();
				}

				// show menu
				showMenu(mtv);			
			}			
		});	    
	}
	



	/**
	 * Promt an alert dialog box for user input
	 * @param point : position we want to create the new marker
	 */
	private void enterTitle(final LatLng point)
    {
    	// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
    	final EditText txtInput = new EditText(this);
    	
    	// Process
    	dialogBuilder.setTitle("Title");
    	dialogBuilder.setMessage("Enter title");
    	dialogBuilder.setView(txtInput);  	
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{   		
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// get user text
				input = txtInput.getText().toString();
				
				// save user input to string title
				title = input;
				
				// promt alert dialog box for description
				enterDescription(point);				
			}
		});
    			
    	// create alert dialog box and show it
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();   	  	
    }
    
    private void enterDescription(final LatLng point)
    {
    	// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
    	final EditText txtInput = new EditText(this);
    	
    	// Process
    	dialogBuilder.setTitle("Description");
    	dialogBuilder.setMessage("Enter description");
    	dialogBuilder.setView(txtInput);  		
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{
    		
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// get user input
				input = txtInput.getText().toString();
				
				// save input
				description = input;
				
				// promt user to choose category
				chooseCategory(point);
			}			
		});
    	
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();  	  	
    }
    
    
    private void chooseCategory(final LatLng point)
    {
    	// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
	
    	// Process
    	dialogBuilder.setTitle("Choose Category");
    	dialogBuilder.setSingleChoiceItems(R.array.category, 0, null);
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				// Create listview with categories
				ListView lw = ((AlertDialog)dialog).getListView();
				Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
				
				// save user choice to category
				category = checkedItem.toString();
							
				// now we check if we want to add a new mark or update an existing one
				if (!update) 
					addMarker(point);
				else
					update();
			}
		});
    		
    	// create dialog box and show it to user
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show(); 	  	
    }
    
    private void readDestination()
    {
    	// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
    	final EditText txtInput = new EditText(this);
    	
    	// Process
    	dialogBuilder.setTitle("Dwse afetiria");
    	dialogBuilder.setMessage("Afetiria : ");
    	dialogBuilder.setView(txtInput);  		
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{		
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// save user input
				input = txtInput.getText().toString();
				
				if(bool2) 
				{
					bool2 = false;
					endPointTitle = input;
				}
				else
				{
					startPointTitle = input;
				}
				
				// calculate directions
				findDirections();
			}

		});
    		
    	// create alert dialog box and show it
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();
    }
    
    /**
     * Finds directions between 2 points
     */
    private void findDirections()
    {
    	Routing routing = null;
    	
    	// Check for the type of direction
    	if (directionsType.equals("Driving"))
    	{
    		routing = new Routing(Routing.TravelMode.DRIVING);
    	}
    	else if (directionsType.equals("Biking"))
    	{
    		routing = new Routing(Routing.TravelMode.BIKING);
    	}
    	else if (directionsType.equals("Walking"))
    	{
    		routing = new Routing(Routing.TravelMode.WALKING);
    	}
    	
    	
		// find startPoint & endPoint position
		for (Marker marker : markerArray.keySet())
		{
			if (marker.getTitle().equalsIgnoreCase(startPointTitle)) 
			{
				startPointPosition = marker.getPosition();
			}
			else if (marker.getTitle().equalsIgnoreCase(endPointTitle))
			{
				endPointPosition = marker.getPosition();
			}
		}
		
		// We used a Debug class to print messages on LogCat for testing
		/*Debug.out("findDirections()");
		Debug.out("StartPointTitle : " + startPointTitle);
		Debug.out("startPointposition : " + startPointPosition);
		Debug.out("endPoint : " + endPointPosition);*/

        routing.registerListener(this);
        routing.execute(startPointPosition, endPointPosition);       
    }
    
    /**
     * Adds a new marker to our map
     * @param point : position we want to create Marker
     */
    public void addMarker(final LatLng point)
    {
    	float markerColor = 0f;
    	
    	// For each category we give a different color to marker
    	if (category.equalsIgnoreCase("Cafe-Bar")) 
    	{
    		markerColor = BitmapDescriptorFactory.HUE_VIOLET;
    	} 
    	else if (category.equalsIgnoreCase("Restaurant"))
    	{
    		markerColor = BitmapDescriptorFactory.HUE_RED;
    	}
    	else if (category.equalsIgnoreCase("Cinema")) 
    	{
    		markerColor = BitmapDescriptorFactory.HUE_GREEN;
    	} 
    	else if (category.equalsIgnoreCase("Shopping Mall")) 
    	{
    		markerColor = BitmapDescriptorFactory.HUE_YELLOW;
    	}

    	// create a new marker in the point position
	    Marker currentMarker = mMap.addMarker(new MarkerOptions()
	 		.position(point)
	 		.title(title)
	 		.snippet(description)
	 		.icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
	 		    
	    // save marker & category to hashmap	
	    markerArray.put(currentMarker, category);
	    	
	    // save marker & category to database
	    dbHandler.saveMarkerToDB(currentMarker, category);
    }
    
 
    /**
     * Load markers from database and show on map
     */
    private void loadMarkers()
    {
    	// position
    	LatLng position;
    	double x;
    	double y;
    	String cat;
    	float markerColor = 0f;

    	// get cursor
    	Cursor cursor = dbHandler.getCursor();

    	// null check
    	if (!cursor.moveToFirst())
    		return;
    	
    	do
    	{
    		// get position
    		x = cursor.getDouble(cursor.getColumnIndex(MyDBHandler.COLUMN_X));
    		y = cursor.getDouble(cursor.getColumnIndex(MyDBHandler.COLUMN_Y));
    		position = new LatLng(x, y);
    		
    		// get marker category
    		cat = cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_CATEGORY));
    		
        	// choose color for each category
        	if (cat.equalsIgnoreCase("Cafe-Bar")) 
        	{
        		markerColor = BitmapDescriptorFactory.HUE_VIOLET;
        	} 
        	else if (cat.equalsIgnoreCase("Restaurant"))
        	{
        		markerColor = BitmapDescriptorFactory.HUE_RED;
        	}
        	else if (cat.equalsIgnoreCase("Cinema")) 
        	{
        		markerColor = BitmapDescriptorFactory.HUE_GREEN;
        	} 
        	else if (cat.equalsIgnoreCase("Shopping Mall"))
        	{
        		markerColor = BitmapDescriptorFactory.HUE_YELLOW;
        	}
    		 		
    		// add loaded Marker to map
    		Marker marker = mMap.addMarker(new MarkerOptions()
    			.position(position)
    			.title(cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_TITLE)))
    			.snippet(cursor.getString(cursor.getColumnIndex(MyDBHandler.COLUMN_DESCRIPTION)))
    			.icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
    			
    		// save marker & category to hashmap
    		markerArray.put(marker, cat);
    				
    	} while (cursor.moveToNext());	
    }
    


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	// Our main menu
	    // Handle item selection
	    switch (item.getItemId())
	    {
	   		 case R.id.add_marker:
	   			 addMark = true;
	   			 return true;
	   			 	   			 
	   		 case R.id.show_markers:
	   			 loadMarkers();
	   			 return true;
	   			
	   		 case R.id.get_direction:
	   			 getDirectionsFromMainMenu();
	   			 return true;
	   			 
	   		 case R.id.clear_map:
	   			 clearMap();
	   			 return true;
	   			 
	   		 case R.id.configuration:

	   			 return true;   		
	    }
	   
	    return false;
    }
    
    /**
     * Clears map
     */
    private void clearMap()
    {
    	mMap.clear();
    }
    
    private void getDirectionsFromMainMenu()
    {
     	// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
    	final EditText txtInput = new EditText(this);
    	
    	// Process
    	dialogBuilder.setTitle("Proorismos");
    	dialogBuilder.setMessage("Proorismos");
    	dialogBuilder.setView(txtInput);  	  	
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{		
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// get user text
				input = txtInput.getText().toString();
				
				// save user input to string title
				endPointTitle = input;
				
				// read destination
				readDestination();
			}

		});

    	// create alert dialog box and show it
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();
    	
    }
   
    /**
     * Initializes google map
     */
    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ( (MapFragment) getFragmentManager().findFragmentById(R.id.mapid)).getMap();
           
            // Check if we were successful in obtaining the map.
            if (mMap != null)
            {
                setUpMap();
            }
        }
    }
   
    /**
     * Add map long click listener
     */
    private void setUpMap()
    {
    	// add long click listener
	    mMap.setOnMapLongClickListener(this); 
    }
    

    @Override
    public void onMapLongClick(LatLng point) 
    {
    	// check if add mark option is selected
    	if(addMark)
    	{   
    		enterTitle(point);
    		addMark = false;	
    	}
    	else
    	{
    		// We print coordinates
        	double latitude = point.latitude;
    		double longtitude = point.longitude;	
    		
    		Toast.makeText(getApplicationContext(), "Your location is\nLat: " + latitude
    				+ " Long : " + longtitude, Toast.LENGTH_LONG
    				).show();
    		
    	}
    }  
    
    
    /**
     * Shows Marker menu
     */
    public void showMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu);
        popup.show();
    }


	@Override
	public boolean onMenuItemClick(MenuItem item)
	{ 
		// Marker Menu
		switch (item.getItemId())
		{	
			case R.id.delete:
				deleteMarker();
				return true;	
		
    		case R.id.edit:
    			update = true;
    			editMarker();
    			return true;
    			
    		case R.id.get_directionp:
    			getDirectionsFromMarkerMenu();
    			return true;
    			
    		case R.id.share:
    			shareOnFB();
    			return true;
		}		
		return false;	
	}
	

	/**
	 * Share on facebook.
	 */
	private void shareOnFB()
	{
		for(Marker marker : markerArray.keySet())
		{
			if (marker.getTitle().equalsIgnoreCase(titleToDelete))
			{
		    	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
		        .setName("Title = " + marker.getTitle() + " Description = " + marker.getSnippet())
		        		.setLink("www.google.gr")
		        .build();
		    	uiHelper.trackPendingDialogCall(shareDialog.present());			
			}
		}
	}
		
	/**
	 * Calls MyDBHandler's method deleteFromDB(String title) in order to delete marker.
	 */
	private void deleteMarker()
	{
		// delete from db
		dbHandler.deleteFromDB(titleToDelete);
		
		// delete from map
		for(Marker marker : markerArray.keySet()) 
		{
			if (marker.getTitle().equalsIgnoreCase(titleToDelete))
				marker.remove();
		}	
	}
	
	/**
	 * Edits current marker
	 */
	public void editMarker()
	{		
		// First we get new user input
		enterTitle(null);
		
	}
	
	/**
	 * Update Marker on database and map.
	 */
	public void update()
	{
		// update database
		dbHandler.update(title, description, category, titleToDelete);
		
		// update marker
		for(Marker marker : markerArray.keySet())
		{
			if(marker.getTitle().equalsIgnoreCase(titleToDelete)) 
			{
				marker.setTitle(title);
				marker.setSnippet(description);
			}
		}
	}
	
	/**
	 * This method gets called from marker menu
	 */
	public void getDirectionsFromMarkerMenu()
	{
		// First we choose the type of direction(driving-walking etc.)
		chooseTypeOfDirections();
	}
	
	/**
	 * Ask user for the type of direction he prefers
	 */
	private void chooseTypeOfDirections()
	{
		// Variables
    	dialogBuilder = new AlertDialog.Builder(this);
 	
    	// Process
    	dialogBuilder.setTitle("Choose Category");
    	dialogBuilder.setSingleChoiceItems(R.array.directions_type, 0, null);
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				ListView lw = ((AlertDialog)dialog).getListView();
				Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
				directionsType = checkedItem.toString();
				
				chooseWayOfDirection();
			}
		});
    		
    	// create dialog box and show it to user
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();
    	  	
	}
	
	/**
	 * Here we choose the way of direction, By GPS
	 * text, or by clicking on a marker 
	 */
	private void chooseWayOfDirection()
	{
		// Variables
    	dialogBuilder = new AlertDialog.Builder(this);

    	// Process
    	dialogBuilder.setTitle("Choose Category");
    	dialogBuilder.setSingleChoiceItems(R.array.way_of_direction, 0, null);
    	dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
    	{	
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				bool = false;
				ListView lw = ((AlertDialog)dialog).getListView();
				Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
				way_of_direction = checkedItem.toString();
				Debug.out(checkedItem);
				
				// 
				if (way_of_direction.equals("By GPS"))
				{
					if (gps.canGetLocation()) 
					{
						Debug.out("GPS");
						double latitude = gps.latitude;
						double longtitude = gps.longitude;	
						
						
						LatLng coordinates = new LatLng(latitude, longtitude);
						title = "MyTitle";
						description = "My Desc";
						category = "Cafe-Bar";
						addMarker(coordinates);
						endPointPosition = coordinates;
						findDirections();
					}
				}
				else if (way_of_direction.equals("By Text"))
				{
					bool2 = true;
					readDestination();
				} 
				else if (way_of_direction.equals("By Choosing Marker"))
				{
					bool = true;
				}
				
			}
							
			});
					
    	// create dialog box and show it to user
    	AlertDialog dialogBox = dialogBuilder.create();
    	dialogBox.show();
    	  	
	}

	@Override
	public void onRoutingFailure() 
	{	
		Toast.makeText(getApplicationContext(), "Your direction could not be created", Toast.LENGTH_LONG).show();		
	}


	@Override
	public void onRoutingStart()
	{	
		Toast.makeText(getApplicationContext(), "Your direction is being created..", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRoutingSuccess(PolylineOptions mPolyOptions, com.directions.route.Route route)
	{
	      PolylineOptions polyoptions = new PolylineOptions();
	      polyoptions.color(Color.BLUE);
	      polyoptions.width(10);
	      polyoptions.addAll(mPolyOptions.getPoints());
	      mMap.addPolyline(polyoptions);	
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback()
	    {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) 
	        {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) 
	        {
	            Log.i("Activity", "Success!");
	        }
	    });
	}
}
   
      
   

  