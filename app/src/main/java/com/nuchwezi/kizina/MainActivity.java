package com.nuchwezi.kizina;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MusicAlbum musicAlbum;

    private static class FRAGMENT_ARGUMENTS {
        public static final String MUSIC_ALBUM = "MUSIC_ALBUM";
    }

    class MusicAlbum {
        public String AlbumName = getString(R.string.album_name);
        public String AlbumArtist = getString(R.string.album_artiste);
        public String[] SongsFiles = getResources().getStringArray(R.array.files);
        public String[] Songs = getResources().getStringArray(R.array.songs);
        public String[] Lyrics = getResources().getStringArray(R.array.lyrics);
        public String[] SongInfo = getResources().getStringArray(R.array.song_info);
        public String[] SongVisualizations = getResources().getStringArray(R.array.visualizations);
        public int[] SongImages = {

                R.drawable.beatiful_mind_pic,
                R.drawable.kampala_night

        };
        public int albumLength = SongsFiles.length;
        private int currentTrackNumber = 0;
        private int currentPlayPosition = 0;
        MediaPlayer mediaPlayer;
        private int AlbumYear = getResources().getInteger(R.integer.album_year);
        private Runnable playCallback;
        private Runnable pauseCallback;
        private ArrayList<Runnable> startCallback = new ArrayList<>();

        public int getCurrentTrackNumber(){
            return currentTrackNumber;
        }

        public void playNextTrack(){
            playTrack((currentTrackNumber+1) == albumLength? 0 : currentTrackNumber+1);
        }

        public void playTrack(int trackNumber) {
            if(trackNumber > musicAlbum.albumLength)
                currentTrackNumber = 0;
            else
                currentTrackNumber = trackNumber;

            try {
                if((mediaPlayer != null)) {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                    }
                }else
                mediaPlayer = new MediaPlayer();

                AssetFileDescriptor descriptor = getAssets().openFd(musicAlbum.SongsFiles[musicAlbum.currentTrackNumber]);
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                mediaPlayer.prepare();
                //mediaPlayer.setVolume(1f, 1f);
                //mediaPlayer.setLooping(true);
                for(Runnable runnable: startCallback){
                    runnable.run();
                }
                playCallback.run();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void pausePlaying(){
            if(mediaPlayer == null)
                return;

            mediaPlayer.pause();
            currentPlayPosition=mediaPlayer.getCurrentPosition();
            pauseCallback.run();
        }

        public void play(){

            if((mediaPlayer == null)) {

                mediaPlayer = new MediaPlayer();

                try {
                    AssetFileDescriptor descriptor = getAssets().openFd(musicAlbum.SongsFiles[musicAlbum.currentTrackNumber]);
                    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();

                    mediaPlayer.prepare();
                }catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }else
                mediaPlayer.seekTo(currentPlayPosition);


for(Runnable runnable: startCallback){
    runnable.run();
}
            mediaPlayer.start();
            playCallback.run();
        }

        public String getNameOfCurrentSong() {
            return Songs[currentTrackNumber];
        }

        @Override
        public String toString() {
            return String.format("%s (%d)", musicAlbum.AlbumName, musicAlbum.AlbumYear);
        }

        public String getCurrentLyrics() {
            return Lyrics[currentTrackNumber];
        }

        public String getCurrentVisualization() {
            return SongVisualizations[currentTrackNumber];
        }

        public String getCurrentMusicInfo() {
            return SongInfo[currentTrackNumber];
        }

        public boolean isPlaying() {
            if(mediaPlayer == null)
                return false;

            return mediaPlayer.isPlaying();
        }

        public void onPlay(Runnable runnable) {
            playCallback = runnable;
        }

        public void onPause(Runnable runnable) {
            pauseCallback = runnable;
        }

        public int getCurrentSongImage() {
            return SongImages[currentTrackNumber];
        }

        public void onStart(Runnable runnable) {
            startCallback.add(runnable);
        }
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;



    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bootStrapMusicAlbum(new MusicAlbum());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),musicAlbum);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNext);
        final FloatingActionButton fabPausePlay = (FloatingActionButton) findViewById(R.id.fabPlayPause);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                musicAlbum.playNextTrack();

                Snackbar.make(view, String.format("Now playing: %s", musicAlbum.getNameOfCurrentSong()), Snackbar.LENGTH_LONG)
                        .show();
                fabPausePlay.setImageResource(R.drawable.pause);

            }
        });


        fabPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(musicAlbum.isPlaying()) {
                    musicAlbum.pausePlaying();
                    Snackbar.make(view, String.format("Paused: %s", musicAlbum.getNameOfCurrentSong()), Snackbar.LENGTH_LONG)
                            .show();
                    fabPausePlay.setImageResource(R.drawable.play_button);

                }else {
                    musicAlbum.play();
                    Snackbar.make(view, String.format("Now Playing: %s", musicAlbum.getNameOfCurrentSong()), Snackbar.LENGTH_LONG)
                            .show();
                    fabPausePlay.setImageResource(R.drawable.pause);
                }
            }
        });


    }

    private void bootStrapMusicAlbum(MusicAlbum album) {
        this.musicAlbum = album;
        setTitle(musicAlbum.toString());
    }

    // MediaPlayer m; /*assume, somewhere in the global scope...*/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    abstract public static class AlbumFragment extends Fragment {

        MusicAlbum musicAlbum;

        public AlbumFragment(MusicAlbum album){
            super();
            musicAlbum = album;
        }
        public AlbumFragment(){
        }
    }


    public static class MusicFragment extends AlbumFragment {

        public MusicFragment(MusicAlbum album) {
            super(album);
        }
        public  MusicFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_music, container, false);
            final WebView animationWebView = (WebView) rootView.findViewById(R.id.webviewMusic);
            animationWebView.getSettings().setJavaScriptEnabled(true);

            musicAlbum.onPlay(new Runnable() {
                @Override
                public void run() {
                    animationWebView.loadUrl(String.format("file:///android_asset/%s",musicAlbum.getCurrentVisualization()));
                }
            });

            musicAlbum.onPause(new Runnable() {
                @Override
                public void run() {
                    animationWebView.loadUrl("file:///android_asset/html/panda/index.html");
                }
            });

            animationWebView.loadUrl("file:///android_asset/html/panda/index.html");

            // disable scroll on touch
            animationWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
            return rootView;
        }
    }

    public static class LyricsFragment extends AlbumFragment {

        public LyricsFragment(MusicAlbum album) {
            super(album);
        }
        public  LyricsFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_lyrics, container, false);
            final MagicTextView lyricsTextView = (MagicTextView) rootView.findViewById(R.id.lyrics);


            musicAlbum.onStart(new Runnable() {
                @Override
                public void run() {
                    lyricsTextView.setText(musicAlbum.getCurrentLyrics());
                }
            });
            return rootView;
        }
    }

    public static class MusicInfoFragment extends AlbumFragment {

        public MusicInfoFragment(MusicAlbum album) {
            super(album);
        }
        public  MusicInfoFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            final MagicTextView lyricsTextView = (MagicTextView) rootView.findViewById(R.id.info);


            musicAlbum.onStart(new Runnable() {
                @Override
                public void run() {
                    lyricsTextView.setText(musicAlbum.getCurrentMusicInfo());
                }
            });
            return rootView;
        }
    }

    public static class MusicGalleryFragment extends AlbumFragment {

        public MusicGalleryFragment(MusicAlbum album) {
            super(album);
        }
        public  MusicGalleryFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
            final ImageView imageView = (ImageView) rootView.findViewById(R.id.imageViewSong);

            musicAlbum.onStart(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(musicAlbum.getCurrentSongImage());
                }
            });
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments;

        public SectionsPagerAdapter(FragmentManager fm, MusicAlbum album) {
            super(fm);

            fragments = new Fragment[]{
                    new MusicFragment(album),
                    new LyricsFragment(album),
                    new MusicInfoFragment(album),
                    new MusicGalleryFragment(album)
            };
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MusicFragment (defined as a static inner class below).
            return fragments[position];
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MUSIC";
                case 1:
                    return "LYRICS";
                case 2:
                    return "INFO";
                case 3:
                    return "GALLERY";
            }
            return null;
        }
    }
}
