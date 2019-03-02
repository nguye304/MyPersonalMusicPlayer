package com.example.personalmusicplayer

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.song_object.view.*

class MainActivity : AppCompatActivity() {

    var SongList = ArrayList<SongInfo>()
    var adapter:MySongAdapter?=null
    var mp: MediaPlayer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CheckUserPermissions()
        //var tracking=mySong()
        //tracking=start()

        adapter=MySongAdapter(SongList)
        SongListView.adapter=adapter;

    }


        inner  class MySongAdapter: BaseAdapter {
            var SongList=ArrayList<SongInfo>()
            constructor(SongList:ArrayList<SongInfo>):super(){
                this.SongList=SongList
            }

            override fun getView(position: Int, pl: View?, p2: ViewGroup?): View {
            val myView = layoutInflater.inflate(R.layout.song_object, null)
            val Song =this.SongList[position]
            myView.songTitle.text = Song.Title
            myView.songArtist.text = Song.ArtistName
            myView.PlayButton.setOnClickListener(View.OnClickListener {
                //play the song
                if (myView.PlayButton.text.equals("Stop")) {
                    mp!!.stop()
                    myView.PlayButton.text = "Start"
                } else {
                    mp = MediaPlayer()
                    try {
                        mp!!.setDataSource(Song.SongLoc)
                        mp!!.prepare()
                        mp!!.start()
                        myView.PlayButton.text = "Stop"
                        seekBar.max = mp!!.duration
                    } catch (ex: Exception) { }
                }
            })
            return myView
        }
        override fun getItem(item: Int): Any {
            return this.SongList[item]
        }
        override fun getItemId(p0: Int): Long {
            return  p0.toLong()
        }
        override fun getCount(): Int {
            return this.SongList.size
        }

    }
    inner  class  mySong():Thread(){
        override fun run() {
            while(true){
                try{
                    Thread.sleep(1000)
                }catch (ex:Exception){}

                runOnUiThread {
                    if (mp!=null){
                        seekBar.progress = mp!!.currentPosition
                    }//if end
                }//runOnUiThread end
            }//while loop end
        }//fun end
    }//inner class end

    fun CheckUserPermissions() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE_ASK_PERMISSIONS)
                    return
                }
            }
            LoadSong()
        }
    //get access to location permission
    val REQUEST_CODE_ASK_PERMISSIONS = 123

    fun onRequestPermissionResult(requestCode:Int, permissions:Array<String>,grantResults: IntArray){
        when (requestCode){
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                LoadSong()
            }else{//permission denied
                Toast.makeText(this,"Access Denied", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        }

    }

    fun LoadSong(){//puts songs into a list
        val allSongsURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC+ "!=0"
        val cursor=contentResolver.query(allSongsURI,null,selection,null,null)

        if(cursor != null){
            if(cursor!!.moveToFirst()){
                do{
                    val SongLoc = cursor !!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val SongArtist = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val SongName = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    SongList.add(SongInfo(SongName,SongArtist,SongLoc))
                }while(cursor!!.moveToNext())
            }
            cursor!!.close()
            adapter=MySongAdapter(SongList)
            SongListView.adapter=adapter;
        }
    }

}
