package com.malkinfo.ecommerceapps.view

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.malkinfo.ecommerceapps.R
import com.malkinfo.ecommerceapps.SecondFragment
import com.malkinfo.ecommerceapps.adapter.CategoryAdapter
import com.malkinfo.ecommerceapps.adapter.SliderAdapter
import com.malkinfo.ecommerceapps.model.CategoryData
import com.malkinfo.ecommerceapps.model.SliderItem
import com.malkinfo.ecommerceapps.model.User
import com.malkinfo.ecommerceapps.uiltels.categoryItemList
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.nava_header.*

class HomeFragment : Fragment() {

    /**drawerList*/
    private lateinit var drawLayout: DrawerLayout
    private lateinit var menuDraw:ImageView
    private lateinit var nuviget:NavigationView

    /**user Data*/
    var user :User? = null
    var database :FirebaseDatabase? = null


    /**categoryList*/
    private lateinit var catList:ArrayList<CategoryData>
    private lateinit var catAdapter:CategoryAdapter
    private lateinit var catRec:RecyclerView

    private lateinit var viewPagerImageSlider :ViewPager2

    /**image Slider*/
    private lateinit var sliderItemList: ArrayList<SliderItem>
    private lateinit var sliderAdapter :SliderAdapter
    private lateinit var sliderHandle:Handler
    private lateinit var sliderRun :Runnable

    /** firebase*/
    var firebaseFireStore:FirebaseFirestore? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initss(view)
    }
    private fun initss(v: View){

        /** set DrawMenu*/
        drawLayout = v.findViewById(R.id.mainDrawer)
        menuDraw = v.findViewById(R.id.profileImgs)
        nuviget = v.findViewById(R.id.navDrawer)


       viewPagerImageSlider= v.findViewById(R.id.view_pagerImageSlider) //not coming keyword viewPagerImageSlider

        /**get User Data Id*/
        database = FirebaseDatabase.getInstance()
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   user = snapshot.getValue(User::class.java)
                    if (user!!.uid.equals(FirebaseAuth.getInstance().uid)){
                        /**set the User Name Image and mobile number*/
                        Glide.with(v.context)
                            .load(user?.phoneImage)
                            .placeholder(R.drawable.avatar)
                            .into(userImg1)

                        Glide.with(requireActivity())
                            .load(user?.phoneImage)
                            .placeholder(R.drawable.avatar)
                            .into(profileImgs)

                        /**set name & number*/
                        userName1.text = user!!.name
                        userMob1.text = user!!.phoneNumber
                    }
                }

                override fun onCancelled(error: DatabaseError) { }

            })
        /**set open Draw*/
        openDraw()
        catgoryData(v)
        sliderView()
    }

    private fun openDraw() {
        menuDraw.setOnClickListener {
            drawLayout.openDrawer(GravityCompat.START)
        }
        nuviget.itemIconTintList = null
    }


    fun catgoryData(v:View)
    {
        catRec = v.findViewById(R.id.categoryRecycler)
        catList = ArrayList()
        categoryItemList(catList)

        catAdapter = CategoryAdapter(
            v.context,catList
        )
        catRec.layoutManager = LinearLayoutManager(
            v.context,LinearLayoutManager.HORIZONTAL,false
        )
        catRec.adapter = catAdapter
    }



    private fun sliderView() {

        sliderItemList = ArrayList()
        firebaseFireStore = FirebaseFirestore.getInstance()
        firebaseFireStore!!.collection("BANNER").get()
            .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot> {
                override fun onComplete(task: Task<QuerySnapshot>) {

                    if (task.isSuccessful) {
                        for (documentSnapShot in task.result!!) {
                            val sliderItem = SliderItem(
                                documentSnapShot.get("sliderimg").toString()
                            )
                            sliderItemList.add(sliderItem)
                        }
                    }
                }

            })

        //set Adapter

        sliderAdapter = SliderAdapter(
            requireActivity(),
            viewPagerImageSlider, sliderItemList
        )

        viewPagerImageSlider.adapter = sliderAdapter
        viewPagerImageSlider.clipToPadding = false
        viewPagerImageSlider.clipChildren = false
        viewPagerImageSlider.offscreenPageLimit = 3

        viewPagerImageSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val composPageTarn = CompositePageTransformer()
        composPageTarn.addTransformer(MarginPageTransformer(40))

        composPageTarn.addTransformer(object :ViewPager2.PageTransformer{
            override fun transformPage(page: View, position: Float) {
                val r :Float = 1-Math.abs(position)
                page.scaleY = 0.85f +r * 0.15f

            }

        })

        viewPagerImageSlider.setPageTransformer(composPageTarn)
        sliderHandle = Handler()
        sliderRun = object : Runnable{
            override fun run() {
                viewPagerImageSlider.setCurrentItem(viewPagerImageSlider.currentItem +1)
            }

        }
        viewPagerImageSlider.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRun)
                sliderHandle.postDelayed(sliderRun,3000)
            }

        })
    }

}
