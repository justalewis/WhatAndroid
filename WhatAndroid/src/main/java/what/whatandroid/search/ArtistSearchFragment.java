package what.whatandroid.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import api.torrents.artist.Artist;
import api.torrents.artist.Releases;
import what.whatandroid.R;
import what.whatandroid.artist.ArtistActivity;
import what.whatandroid.callbacks.OnLoggedInCallback;
import what.whatandroid.callbacks.SetTitleCallback;

/**
 * Fragment for searching for artists. If only one artist name is returned from the search
 * we view that artist, if multiple ones are returned we go to a torrent search with the search term
 */
public class ArtistSearchFragment extends Fragment
	implements View.OnClickListener, TextView.OnEditorActionListener, OnLoggedInCallback {
	/**
	 * Search terms sent to us by the intent
	 */
	private String searchTerms;
	/**
	 * The loaded artist if we found one, this is used so that the Artist Activity can pick up
	 * the loaded artist without having to re-download it
	 */
	private static Artist artist;
	private static Releases releases;
	private LoadArtistSearch loadArtistSearch;
	/**
	 * The search input box and loading indicator
	 */
	private EditText editTerms;
	private ProgressBar loadingIndicator;

	/**
	 * Create an artist search fragment and have it start loading the search desired when the view
	 * is resumed. If the terms are empty then no search will be launched at load
	 * @param terms terms to run search with. If empty no search will be launched
	 * @return Artist Search fragment viewing the search results, or ready to take input
	 */
	public static ArtistSearchFragment newInstance(String terms){
		ArtistSearchFragment f = new ArtistSearchFragment();
		f.searchTerms = terms;
		return f;
	}

	public ArtistSearchFragment(){
		//Required empty ctor
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
			SetTitleCallback callback = (SetTitleCallback)activity;
			callback.setTitle("Artist Search");
		}
		catch (ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement SetTitleCallback");
		}
	}

	@Override
	public void onDetach(){
		super.onDetach();
		if (loadArtistSearch != null){
			loadArtistSearch.cancel(true);
		}
	}

	@Override
	public void onLoggedIn(){
		//Artist search fragment really shouldn't auto-search, since it only re-directs to new fragments
		//based on the result
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
		Button searchButton = (Button)view.findViewById(R.id.search_button);
		searchButton.setOnClickListener(this);
		editTerms = (EditText)view.findViewById(R.id.search_terms);
		editTerms.setOnEditorActionListener(this);
		loadingIndicator = (ProgressBar)view.findViewById(R.id.loading_indicator);

		if (searchTerms == null || searchTerms.isEmpty()){
			loadingIndicator.setVisibility(View.GONE);
		}
		else {
			editTerms.setText(searchTerms);
		}
		return view;
	}

	@Override
	public void onClick(View v){
		searchTerms = editTerms.getText().toString();
		if (!searchTerms.isEmpty()){
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromInputMethod(editTerms.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			if (loadArtistSearch != null){
				loadArtistSearch.cancel(true);
			}
			loadArtistSearch = new LoadArtistSearch();
			loadArtistSearch.execute(searchTerms);
		}
		else {
			Toast.makeText(getActivity(), "Enter search terms", Toast.LENGTH_SHORT).show();
			editTerms.requestFocus();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
		if (event == null || event.getAction() == KeyEvent.ACTION_DOWN){
			searchTerms = editTerms.getText().toString();
			if (!searchTerms.isEmpty()){
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromInputMethod(editTerms.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
				if (loadArtistSearch != null){
					loadArtistSearch.cancel(true);
				}
				loadArtistSearch = new LoadArtistSearch();
				loadArtistSearch.execute(searchTerms);
			}
			else {
				Toast.makeText(getActivity(), "Enter search terms", Toast.LENGTH_SHORT).show();
				editTerms.requestFocus();
			}
		}
		return true;
	}

	/**
	 * Get the loaded artist from the search
	 * @return the loaded artist from the search
	 */
	public static Artist getArtist(){
		return artist;
	}

	/**
	 * Get the loaded releases from the search
	 * @return the loaded releases
	 */
	public static Releases getReleases(){
		return releases;
	}

	/**
	 * Load the artist auto completions for some artist name as a "search". If only one completion
	 * is returned we launch an intent to view that artist, if multiple completions are returned we
	 * launch a torrent search with the same terms, as the site does.
	 */
	private class LoadArtistSearch extends AsyncTask<String, Void, Artist> {
		/**
		 * Artist name we're trying to load
		 */
		String name;

		@Override
		protected void onPreExecute(){
			if (loadingIndicator != null){
				loadingIndicator.setVisibility(View.VISIBLE);
			}
			artist = null;
			releases = null;
		}

		@Override
		protected Artist doInBackground(String... params){
			name = params[0];
			try {
				Artist a = Artist.fromName(params[0]);
				if (a != null){
					releases = new Releases(a);
					return a;
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Artist a){
			if (loadingIndicator != null){
				loadingIndicator.setVisibility(View.GONE);
			}
			if (a != null){
				artist = a;
				Intent intent = new Intent(getActivity(), ArtistActivity.class);
				intent.putExtra(ArtistActivity.ARTIST_ID, artist.getId());
				intent.putExtra(ArtistActivity.USE_SEARCH, true);
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(getActivity(), SearchActivity.class);
				intent.putExtra(SearchActivity.SEARCH, SearchActivity.TORRENT);
				intent.putExtra(SearchActivity.TERMS, name);
				startActivity(intent);
			}
		}
	}
}