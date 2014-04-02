package what.whatandroid.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import api.soup.MySoup;
import what.whatandroid.R;
import what.whatandroid.announcements.AnnouncementsActivity;
import what.whatandroid.callbacks.OnLoggedInCallback;
import what.whatandroid.callbacks.ViewRequestCallbacks;
import what.whatandroid.callbacks.ViewTorrentCallbacks;
import what.whatandroid.callbacks.ViewUserCallbacks;
import what.whatandroid.login.LoggedInActivity;
import what.whatandroid.profile.ProfileActivity;
import what.whatandroid.request.RequestActivity;
import what.whatandroid.torrentgroup.TorrentGroupActivity;

/**
 * Activity for performing Torrent, User or Request searches. The searching itself is handled
 * by the specific fragments
 */
public class SearchActivity extends LoggedInActivity
	implements ViewTorrentCallbacks, ViewUserCallbacks, ViewRequestCallbacks, OnLoggedInCallback {
	/**
	 * Param to pass the search type desired and terms and tags if desired
	 */
	public static final String SEARCH = "what.whatandroid.SEARCH", TERMS = "what.whatandroid.SEARCH.TERMS",
		TAGS = "what.whatandroid.SEARCH.TAGS";
	/**
	 * The parameters to specify what we want to search for
	 */
	public static final int TORRENT = 0, ARTIST = 1, USER = 2, REQUEST = 3;
	private int type;
	/**
	 * The OnLoggedInCallback for the search fragment, so we can tell it that it's ok to start loading
	 * an existing search if it has one
	 */
	private OnLoggedInCallback searchFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frame);
		setupNavDrawer();
		setTitle(getTitle());

		if (savedInstanceState != null){
			type = savedInstanceState.getInt(SEARCH);
		}
		else {
			type = getIntent().getIntExtra(SEARCH, TORRENT);
		}
		String terms = getIntent().getStringExtra(TERMS);
		String tags = getIntent().getStringExtra(TAGS);

		Fragment fragment;
		switch (type){
			case ARTIST:
				fragment = ArtistSearchFragment.newInstance(terms);
				break;
			case USER:
				fragment = UserSearchFragment.newInstance(terms);
				break;
			case REQUEST:
				fragment = RequestSearchFragment.newInstance(terms, tags);
				break;
			default:
				fragment = TorrentSearchFragment.newInstance(terms, tags);
				break;
		}
		searchFragment = (OnLoggedInCallback)fragment;
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().add(R.id.container, fragment).commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		//Eventually also save the terms, tags & maybe search results?
		//maybe only first page of results to speed things up
		outState.putInt(SEARCH, type);
	}

	@Override
	public void onLoggedIn(){
		searchFragment.onLoggedIn();
	}

	@Override
	public void viewTorrentGroup(int id){
		Intent intent = new Intent(this, TorrentGroupActivity.class);
		intent.putExtra(TorrentGroupActivity.GROUP_ID, id);
		startActivity(intent);
	}

	@Override
	public void viewUser(int id){
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(ProfileActivity.USER_ID, id);
		startActivity(intent);
	}

	@Override
	public void viewRequest(int id){
		Intent intent = new Intent(this, RequestActivity.class);
		intent.putExtra(RequestActivity.REQUEST_ID, id);
		startActivity(intent);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position){
		if (navDrawer == null){
			return;
		}
		String selection = navDrawer.getItem(position);
		if (selection.equalsIgnoreCase(getString(R.string.announcements))){
			//Launch AnnouncementsActivity viewing announcements
			//For now both just return to the announcements view
			Intent intent = new Intent(this, AnnouncementsActivity.class);
			intent.putExtra(AnnouncementsActivity.SHOW, AnnouncementsActivity.ANNOUNCEMENTS);
			startActivity(intent);
		}
		else if (selection.equalsIgnoreCase(getString(R.string.blog))){
			//Launch AnnouncementsActivity viewing blog posts
			Intent intent = new Intent(this, AnnouncementsActivity.class);
			intent.putExtra(AnnouncementsActivity.SHOW, AnnouncementsActivity.BLOGS);
			startActivity(intent);
		}
		else if (selection.equalsIgnoreCase(getString(R.string.profile))){
			//Launch profile view activity
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.putExtra(ProfileActivity.USER_ID, MySoup.getUserId());
			startActivity(intent);
		}
		else if (selection.equalsIgnoreCase(getString(R.string.torrents)) && type != TORRENT){
			FragmentManager fm = getSupportFragmentManager();
			TorrentSearchFragment f = new TorrentSearchFragment();
			searchFragment = f;
			fm.beginTransaction().replace(R.id.container, f).commit();
			type = TORRENT;
		}
		else if (selection.equalsIgnoreCase(getString(R.string.artists)) && type != ARTIST){
			FragmentManager fm = getSupportFragmentManager();
			ArtistSearchFragment f = new ArtistSearchFragment();
			searchFragment = f;
			fm.beginTransaction().replace(R.id.container, f).commit();
			type = ARTIST;
		}
		else if (selection.equalsIgnoreCase(getString(R.string.requests)) && type != REQUEST){
			FragmentManager fm = getSupportFragmentManager();
			RequestSearchFragment f = new RequestSearchFragment();
			searchFragment = f;
			fm.beginTransaction().replace(R.id.container, f).commit();
			type = REQUEST;
		}
		else if (selection.equalsIgnoreCase(getString(R.string.users)) && type != USER){
			FragmentManager fm = getSupportFragmentManager();
			UserSearchFragment f = new UserSearchFragment();
			searchFragment = f;
			fm.beginTransaction().replace(R.id.container, f).commit();
			type = USER;
		}
	}
}