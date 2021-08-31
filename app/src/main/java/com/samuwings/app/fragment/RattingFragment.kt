package com.samuwings.app.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samuwings.app.R
import com.samuwings.app.activity.DashboardActivity
import com.samuwings.app.activity.LoginActivity
import com.samuwings.app.api.ApiClient
import com.samuwings.app.api.ListResponse
import com.samuwings.app.api.SingleResponse
import com.samuwings.app.base.BaseAdaptor
import com.samuwings.app.base.BaseFragmnet
import com.samuwings.app.model.RattingModel
import com.samuwings.app.utils.Common
import com.samuwings.app.utils.Common.alertErrorOrValidationDialog
import com.samuwings.app.utils.Common.dismissLoadingProgress
import com.samuwings.app.utils.Common.showLoadingProgress
import com.samuwings.app.utils.SharePreference
import kotlinx.android.synthetic.main.dlg_write_review.view.*
import kotlinx.android.synthetic.main.fragment_ratting.*
import kotlinx.android.synthetic.main.fragment_ratting.ivMenu
import kotlinx.android.synthetic.main.fragment_ratting.swiperefresh
import kotlinx.android.synthetic.main.fragment_ratting.tvNoDataFound
import kotlinx.android.synthetic.main.row_ratting.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class RattingFragment:BaseFragmnet() {
    var rattingList:ArrayList<RattingModel>?=null
    override fun setView(): Int {
       return R.layout.fragment_ratting
    }

    override fun Init(view: View) {
        Common.getCurrentLanguage(activity!!, false)
        rattingList= ArrayList()
        if(Common.isCheckNetwork(activity!!)){
            callApiRatting(false)
        }else{
            alertErrorOrValidationDialog(
                activity!!,
                resources.getString(R.string.no_internet)
            )
        }

        ivMenu.setOnClickListener {
            (activity as DashboardActivity?)!!.onDrawerToggle()
        }
        ivAddWallpaper.setOnClickListener {
            if(SharePreference.getBooleanPref(activity!!,SharePreference.isLogin)){
                mWriteReviewDialog(activity!!)
            }else {
                openActivity(LoginActivity::class.java)
                activity!!.finish()
            }

        }

        swiperefresh.setOnRefreshListener { // Your code to refresh the list here.
            rattingList= ArrayList()
            if(Common.isCheckNetwork(activity!!)){
                swiperefresh.isRefreshing=false
                callApiRatting(false)
            }else{
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }


    private fun callApiRatting(isReview:Boolean) {
        if(!isReview){
            showLoadingProgress(activity!!)
        }else{
            rattingList!!.clear()
        }
        val call = ApiClient.getClient.getRatting()
        call.enqueue(object : Callback<ListResponse<RattingModel>> {
            override fun onResponse(
                call: Call<ListResponse<RattingModel>>,
                response: Response<ListResponse<RattingModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: ListResponse<RattingModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            rvRatting.visibility=View.VISIBLE
                            tvNoDataFound.visibility=View.GONE
                            for(i in 0 until restResponce.getData().size){
                                val retting=RattingModel()
                                retting.setName(restResponce.getData().get(i).getName())
                                retting.setRatting(restResponce.getData().get(i).getRatting())
                                retting.setComment(restResponce.getData().get(i).getComment())
                                retting.setCreated_at(restResponce.getData().get(i).getCreated_at())
                                rattingList!!.add(retting)
                            }
                            setFoodCategoryAdaptor(rattingList!!)
                        }else{
                            rvRatting.visibility=View.GONE
                            tvNoDataFound.visibility=View.VISIBLE
                        }
                    }else if(restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        rvRatting.visibility=View.GONE
                        tvNoDataFound.visibility=View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<ListResponse<RattingModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    fun setFoodCategoryAdaptor(rattingList:ArrayList<RattingModel>) {
        val rattingAdapter = object : BaseAdaptor<RattingModel>(activity!!, rattingList) {
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: RattingModel,
                position: Int
            ) {
                holder!!.itemView.tvRattingName.text=rattingList.get(position).getName()
                holder.itemView.tvRattingDate.text=rattingList.get(position).getCreated_at()
                holder.itemView.tvRattingDiscription.text=rattingList.get(position).getComment()

                if(rattingList.get(position).getRatting().equals("1")){
                    holder.itemView.ivRatting.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ratting1,null))
                }else if(rattingList.get(position).getRatting().equals("2")){
                    holder.itemView.ivRatting.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ratting2,null))
                }else if(rattingList.get(position).getRatting().equals("3")){
                    holder.itemView.ivRatting.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ratting3,null))
                }else if(rattingList.get(position).getRatting().equals("4")){
                    holder.itemView.ivRatting.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ratting4,null))
                }else if(rattingList.get(position).getRatting().equals("5")){
                    holder.itemView.ivRatting.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ratting5,null))
                }
            }
            override fun setItemLayout(): Int {
                return R.layout.row_ratting
            }
            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvRatting.adapter = rattingAdapter
        rvRatting.layoutManager = LinearLayoutManager(activity!!)
        rvRatting.itemAnimator = DefaultItemAnimator()
        rvRatting.isNestedScrollingEnabled = true
    }

    @SuppressLint("NewApi")
    fun mWriteReviewDialog(act: Activity) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            val m_inflater = LayoutInflater.from(act)
            val m_view = m_inflater.inflate(R.layout.dlg_write_review, null, false)
            dialog.setContentView(m_view)
            val finalDialog: Dialog = dialog
            val edDiscription = m_view.findViewById<EditText>(R.id.edDescription)
            val ivStar1 = m_view.findViewById<ImageView>(R.id.ivStar1)
            val ivStar2 = m_view.findViewById<ImageView>(R.id.ivStar2)
            val ivStar3 = m_view.findViewById<ImageView>(R.id.ivStar3)
            val ivStar4 = m_view.findViewById<ImageView>(R.id.ivStar4)
            val ivStar5 = m_view.findViewById<ImageView>(R.id.ivStar5)

            var temp = 0
            var rattingValue:Int =1;
            ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
            ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
            ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
            ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
            ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
            ivStar1.setOnClickListener {
                ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                rattingValue=1;
            }
            ivStar2.setOnClickListener {
                ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                rattingValue=2;
            }
            ivStar3.setOnClickListener {
                ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                rattingValue=3;
            }
            ivStar4.setOnClickListener {
                ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.gray))
                rattingValue=4;
            }
            ivStar5.setOnClickListener {
                ivStar1.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar2.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar3.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar4.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                ivStar5.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!,R.color.colorPrimary))
                rattingValue=5;
            }

            m_view.tvUpdate.setOnClickListener {
                if(edDiscription.text.toString().equals("")){
                   alertErrorOrValidationDialog(act,resources.getString(R.string.validation_comment))
                }else{
                    if (Common.isCheckNetwork(act)) {
                        finalDialog.dismiss()
                        callApiPutRatting(rattingValue.toString(),edDiscription.text.toString())
                    } else {
                        alertErrorOrValidationDialog(act, resources.getString(R.string.no_internet))
                    }
                }
            }

            if (!act.isFinishing) dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun callApiPutRatting(rattingValue: String, discription: String) {
        showLoadingProgress(activity!!)
        val map = HashMap<String, String>()
        map.put("user_id", SharePreference.getStringPref(
            activity!!,
            SharePreference.userId
        )!!)
        map.put("ratting",rattingValue)
        map.put("comment",discription)
        val call = ApiClient.getClient.setRatting(map)
        call.enqueue(object : Callback<SingleResponse> {
            @SuppressLint("NewApi")
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {

                if (response.code() == 200) {
                    val restResponce: SingleResponse = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        val c: Calendar = Calendar.getInstance()
                        System.out.println("Current time => " + c.getTime())
                        val df = SimpleDateFormat("dd MMM yyyy")
                        val formattedDate: String = df.format(c.getTime())
                        if(Common.isCheckNetwork(activity!!)){
                            callApiRatting(true)
                        }else{
                            alertErrorOrValidationDialog(
                                activity!!,
                                resources.getString(R.string.no_internet)
                            )
                        }

                    }
                }else{
                    dismissLoadingProgress()
                    val jsonObj:JSONObject=JSONObject(response.errorBody()!!.string())
                    if (jsonObj.getInt("status")==0) {
                        alertErrorOrValidationDialog(
                            activity!!,
                            jsonObj.getString("message")
                        )
                    }
                }
            }
            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    activity!!,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(activity!!, false)
    }
}