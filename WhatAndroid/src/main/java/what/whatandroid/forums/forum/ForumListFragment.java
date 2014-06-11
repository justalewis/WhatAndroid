package what.whatandroid.forums.forum;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import api.forum.forum.Forum;
import api.soup.MySoup;
import what.whatandroid.R;
import what.whatandroid.callbacks.LoadingListener;
import what.whatandroid.callbacks.OnLoggedInCallback;
import what.whatandroid.forums.ForumActivity;

/**
 * Fragment that displays a list of the forum threads on some page of the forum
 */
public class ForumListFragment extends Fragment implements OnLoggedInCallback, LoaderManager.LoaderCallbacks<Forum> {
	private LoadingListener<Forum> listener;
	private ProgressBar loadingIndicator;
	private ForumListAdapter adapter;

	/**
	 * Get a fragment displaying the list of posts at some page in the forum
	 *
	 * @param forum forum id to view
	 * @param page  page of posts to display
	 */
	public static ForumListFragment newInstance(int forum, int page){
		ForumListFragment f = new ForumListFragment();
		Bundle args = new Bundle();
		args.putInt(ForumActivity.FORUM_ID, forum);
		args.putInt(ForumActivity.PAGE, page);
		f.setArguments(args);
		return f;
	}

	public ForumListFragment(){
		//Required empty ctor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_list_view, container, false);
		ListView list = (ListView)view.findViewById(R.id.list);
		loadingIndicator = (ProgressBar)view.findViewById(R.id.loading_indicator);
		adapter = new ForumListAdapter(getActivity());
		list.setAdapter(adapter);
		list.setOnItemClickListener(adapter);
		if (MySoup.isLoggedIn()){
			getLoaderManager().initLoader(0, getArguments(), this);
		}
		return view;
	}

	/**
	 * Set a listener to be called with the loaded forum data once it's loaded
	 */
	public void setListener(LoadingListener<Forum> listener){
		this.listener = listener;
	}

	@Override
	public void onLoggedIn(){
		if (isAdded()){
			getLoaderManager().initLoader(0, getArguments(), this);
		}
	}

	@Override
	public Loader<Forum> onCreateLoader(int id, Bundle args){
		loadingIndicator.setVisibility(View.VISIBLE);
		return new ForumAsyncLoader(getActivity(), args);
	}

	@Override
	public void onLoadFinished(Loader<Forum> loader, Forum data){
		loadingIndicator.setVisibility(View.GONE);
		if (data == null || data.getResponse() == null || !data.getStatus()){
			Toast.makeText(getActivity(), "Could not load page", Toast.LENGTH_LONG).show();
		}
		else {
			if (adapter.isEmpty()){
				adapter.addAll(data.getThreads());
				adapter.notifyDataSetChanged();
				if (listener != null){
					listener.onLoadingComplete(data);
				}
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Forum> loader){
		adapter.clear();
		adapter.notifyDataSetChanged();
	}
}