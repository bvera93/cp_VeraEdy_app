package com.samuwings.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.samuwings.app.R
import com.samuwings.app.api.ApiClient
import com.samuwings.app.api.ListResponse
import com.samuwings.app.api.SingleResponse
import com.samuwings.app.base.BaseActivity
import com.samuwings.app.base.BaseAdaptor
import com.samuwings.app.model.CartItemModel
import com.samuwings.app.utils.Common
import com.samuwings.app.utils.Common.alertErrorOrValidationDialog
import com.samuwings.app.utils.Common.dismissLoadingProgress
import com.samuwings.app.utils.Common.getCurrentLanguage
import com.samuwings.app.utils.Common.getLog
import com.samuwings.app.utils.Common.isCheckNetwork
import com.samuwings.app.utils.Common.showLoadingProgress
import com.samuwings.app.utils.SharePreference
import com.samuwings.app.utils.SharePreference.Companion.getStringPref
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.row_cart.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CartActivity : BaseActivity() {
    var cartItemAdapter:BaseAdaptor<CartItemModel>?=null
    var cartItem:ArrayList<CartItemModel>?=ArrayList()
    override fun setLayout(): Int {
        return R.layout.activity_cart
    }

    override fun InitView() {
        getCurrentLanguage(this@CartActivity,false)
        tvCheckout.visibility = View.GONE
        if (isCheckNetwork(this@CartActivity)) {
          callApiCart(false)
        } else {
          alertErrorOrValidationDialog(
            this@CartActivity,
            resources.getString(R.string.no_internet)
          )
        }

        ivBack.setOnClickListener {
            finish()
        }

        ivHome.setOnClickListener {
            val intent=Intent(this@CartActivity,DashboardActivity::class.java).putExtra("pos","1")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        tvCheckout.setOnClickListener {
            if(isCheckNetwork(this@CartActivity)){
                callApiIsOpen()
            }else{
                alertErrorOrValidationDialog(this@CartActivity,resources.getString(R.string.no_internet))
            }
        }
    }


    private fun callApiCart(isQty: Boolean) {
        if(!isQty){
            showLoadingProgress(this@CartActivity)
        }
        val map = HashMap<String, String>()
        map.put("user_id", getStringPref(this@CartActivity, SharePreference.userId)!!)
        val call = ApiClient.getClient.getCartItem(map)
        call.enqueue(object : Callback<ListResponse<CartItemModel>> {
            override fun onResponse(
                call: Call<ListResponse<CartItemModel>>,
                response: Response<ListResponse<CartItemModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: ListResponse<CartItemModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            rvCartFood.visibility = View.VISIBLE
                            tvNoDataFound.visibility = View.GONE
                            tvCheckout.visibility = View.VISIBLE
                            cartItem=restResponce.getData()
                            setFoodCartAdaptor(cartItem!!)
                        } else {
                            rvCartFood.visibility = View.GONE
                            tvNoDataFound.visibility = View.VISIBLE
                            tvCheckout.visibility = View.GONE
                        }
                    }
                }else{
                    dismissLoadingProgress()
                    rvCartFood.visibility = View.GONE
                    tvNoDataFound.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ListResponse<CartItemModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@CartActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
    @SuppressLint("ResourceType", "NewApi")
    private fun setFoodCartAdaptor(cartItemList: ArrayList<CartItemModel>) {
        cartItemAdapter = object : BaseAdaptor<CartItemModel>(this@CartActivity, cartItem!!) {
            @SuppressLint("SetTextI18n")
            override fun onBindData(
              holder: RecyclerView.ViewHolder?,
              `val`: CartItemModel,
              position: Int
            ) {
                holder!!.itemView.tvFoodName.text = cartItem!!.get(position).getItem_name()
                holder.itemView.tvFoodPrice.text =Common.getCurrancy(this@CartActivity)+String.format(Locale.US,"%,.2f",cartItem!!.get(position).getPrice()!!.toDouble())
                holder.itemView.tvFoodQty.text = cartItem!!.get(position).getQty()
                Glide.with(this@CartActivity).load(cartItem!!.get(position).getItemimage()!!.getImage()).into( holder.itemView.ivFoodCart)

                if(cartItem!!.get(position).getAddons_id().equals("")||cartItem!!.get(position).getAddons_id()==null){
                    holder.itemView.tvAddons.backgroundTintList= ColorStateList.valueOf(resources.getColor(R.color.gray))
                }else{
                    holder.itemView.tvAddons.backgroundTintList= ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                }
                if(cartItem!!.get(position).getItem_notes()==null){
                  holder.itemView.tvNotes.backgroundTintList= ColorStateList.valueOf(resources.getColor(R.color.gray))
                }else{
                  holder.itemView.tvNotes.backgroundTintList= ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                }

                holder.itemView.tvAddons.setOnClickListener {
                  if(cartItem!!.get(position).getAddons().size>0){
                    Common.openDialogSelectedAddons(this@CartActivity,cartItem!!.get(position).getAddons())
                  }
                }

                holder.itemView.tvNotes.setOnClickListener {
                  if(cartItem!!.get(position).getItem_notes()!=null){
                    Common.alertNotesDialog(this@CartActivity,cartItem!!.get(position).getItem_notes())
                  }
                }

                holder.itemView.ivDeleteCartItem.setOnClickListener {
                  if (isCheckNetwork(this@CartActivity)) {
                    dlgDeleteConformationDialog(this@CartActivity,"Est?? seguro de eliminar este producto ",cartItem!!.get(position).getId()!!,position)
                  } else {
                    alertErrorOrValidationDialog(
                        this@CartActivity,
                        resources.getString(R.string.no_internet)
                    )
                  }
                }

                holder.itemView.ivMinus.setOnClickListener {
                  if(cartItem!!.get(position).getQty()!!.toInt() > 1){
                    holder.itemView.ivMinus.isClickable =true
                    getLog("Qty>>",cartItem!!.get(position).getQty().toString())
                    if (isCheckNetwork(this@CartActivity)) {
                      callApiCartQTYUpdate(cartItemList.get(position),position,false)
                    } else {
                      alertErrorOrValidationDialog(
                        this@CartActivity,
                        resources.getString(R.string.no_internet)
                      )
                    }
                  }else{
                    holder.itemView.ivMinus.isClickable =false
                    getLog("Qty1>>",cartItem!!.get(position).getQty().toString())
                  }
                }
                holder.itemView.ivPlus.setOnClickListener {
                    if(cartItem!!.get(position).getQty()!!.toInt()<getStringPref(this@CartActivity,SharePreference.isMiniMumQty)!!.toInt()){
                      if (isCheckNetwork(this@CartActivity)) {
                        callApiCartQTYUpdate(cartItemList.get(position),position,true)
                      } else {
                        alertErrorOrValidationDialog(
                          this@CartActivity,
                          resources.getString(R.string.no_internet)
                        )
                      }
                    }else{
                      alertErrorOrValidationDialog(this@CartActivity,"Maximum quantity allowed ${getStringPref(this@CartActivity,SharePreference.isMiniMumQty)}")
                    }
                }
            }
            override fun setItemLayout(): Int {
                return R.layout.row_cart
            }

            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvCartFood.adapter = cartItemAdapter
        rvCartFood.layoutManager = LinearLayoutManager(this@CartActivity)
        rvCartFood.itemAnimator = DefaultItemAnimator()
        rvCartFood.isNestedScrollingEnabled = true
    }

    private fun callApiCartQTYUpdate(
        cartModel: CartItemModel,
        pos: Int,
        isPlus: Boolean
    ) {
        var qty=0
        if(isPlus){
            qty=cartModel.getQty()!!.toInt()+1
        }else{
            qty=cartModel.getQty()!!.toInt()-1
        }
        showLoadingProgress(this@CartActivity)
        val map = HashMap<String, String>()
        map.put("cart_id",cartModel.getId()!!)
        map.put("item_id",cartModel.getItem_id()!!)
        map.put("qty",qty.toString())
        map.put("user_id", getStringPref(this@CartActivity, SharePreference.userId)!!)
        val call = ApiClient.getClient.setQtyUpdate(map)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    val restResponce: SingleResponse = response.body()!!
                    if(restResponce.getStatus().equals("1")){
                        callApiCart(true)
                    }else{
                        dismissLoadingProgress()
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@CartActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
    private fun callApiCartItemDelete(strCartId:String,pos:Int) {
        showLoadingProgress(this@CartActivity)
        val map = HashMap<String, String>()
        map.put("cart_id",strCartId)
        val call = ApiClient.getClient.setDeleteCartItem(map)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: SingleResponse = response.body()!!
                    if(restResponce.getStatus().equals("1")){
                        Common.isCartTrue=true
                        Common.isCartTrueOut=true
                        Common.showSuccessFullMsg(this@CartActivity,restResponce.getMessage()!!)
                        cartItem!!.removeAt(pos)
                        cartItemAdapter!!.notifyDataSetChanged()
                        if(cartItem!!.size>0){
                            tvCheckout.visibility=View.VISIBLE
                        }else{
                            tvCheckout.visibility=View.GONE
                            rvCartFood.visibility = View.GONE
                            tvNoDataFound.visibility = View.VISIBLE
                            tvCheckout.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@CartActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    fun dlgDeleteConformationDialog(act: Activity, msg: String?,strCartId: String,pos:Int) {
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
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(act)
            val m_view = m_inflater.inflate(R.layout.dlg_confomation, null, false)
            val textDesc: TextView = m_view.findViewById(R.id.tvDesc)
            textDesc.text = msg
            val tvOk: TextView = m_view.findViewById(R.id.tvYes)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                if (isCheckNetwork(this@CartActivity)) {
                    finalDialog.dismiss()
                    callApiCartItemDelete(strCartId,pos)
                } else {
                    alertErrorOrValidationDialog(
                        this@CartActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
            }
            val tvCancle: TextView = m_view.findViewById(R.id.tvNo)
            tvCancle.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun successFullDeleteDialog(act: Activity, msg: String?,pos:Int) {
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
            dialog.setCancelable(false)
            val m_inflater = LayoutInflater.from(act)
            val m_view = m_inflater.inflate(R.layout.dlg_validation, null, false)
            val textDesc: TextView = m_view.findViewById(R.id.tvMessage)
            textDesc.text = msg
            val tvOk: TextView = m_view.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                cartItem!!.removeAt(pos)
                cartItemAdapter!!.notifyDataSetChanged()
                if(cartItem!!.size>0){
                    tvCheckout.visibility=View.VISIBLE
                }else{
                    tvCheckout.visibility=View.GONE
                    rvCartFood.visibility = View.GONE
                    tvNoDataFound.visibility = View.VISIBLE
                    tvCheckout.visibility = View.GONE
                }
                finalDialog.dismiss()
            }
            dialog.setContentView(m_view)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@CartActivity, false)
    }

    private fun callApiIsOpen() {
        showLoadingProgress(this@CartActivity)
        val call = ApiClient.getClient.getCheckStatusRestaurant()
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                if (response.code() == 200) {
                    val restResponce: SingleResponse = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        dismissLoadingProgress()
                        startActivity(Intent(this@CartActivity,OrderSummuryActivity::class.java))
                    } else if (restResponce.getStatus()!!.equals("0")) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@CartActivity,
                            restResponce.getMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@CartActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }
}
