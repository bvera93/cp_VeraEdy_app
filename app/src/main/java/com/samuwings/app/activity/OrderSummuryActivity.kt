package com.samuwings.app.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.ClipboardManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samuwings.app.R
import com.samuwings.app.api.*
import com.samuwings.app.api.ApiClient.MapKey
import com.samuwings.app.base.BaseActivity
import com.samuwings.app.base.BaseAdaptor
import com.samuwings.app.model.GetPromocodeModel
import com.samuwings.app.model.OrderSummaryModel
import com.samuwings.app.model.PromocodeModel
import com.samuwings.app.model.SummaryModel
import com.samuwings.app.utils.Common
import com.samuwings.app.utils.Common.alertErrorOrValidationDialog
import com.samuwings.app.utils.Common.dismissLoadingProgress
import com.samuwings.app.utils.FieldSelector
import com.samuwings.app.utils.SharePreference.Companion.getStringPref
import com.samuwings.app.utils.SharePreference.Companion.isCurrancy
import com.samuwings.app.utils.SharePreference.Companion.userId
import com.bumptech.glide.Glide
import com.samuwings.app.utils.SharePreference.Companion.isMaximum
import com.samuwings.app.utils.SharePreference.Companion.isMiniMum
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_yoursorderdetail.*
import kotlinx.android.synthetic.main.row_orderitemsummary.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OrderSummuryActivity:BaseActivity() {
    var summaryModel=SummaryModel()
    var promocodeList:ArrayList<PromocodeModel>?=null
    var discountAmount="0.00"
    var discountPer="0"
    var promocodePrice:Float= 0.0F
    var lat:Double=0.0
    var lon:Double=-0.0
    var select_Delivery=1
    var city=""
    var state=""
    var country_code=""
    var postal_code=""
    var AUTOCOMPLETE_REQUEST_CODE: Int = 2
    var fieldSelector: FieldSelector? = null
    override fun setLayout(): Int {
       return R.layout.activity_yoursorderdetail
    }

    @SuppressLint("SetTextI18n")
    override fun InitView() {
        promocodeList=ArrayList()
        Places.initialize(applicationContext,MapKey)
        fieldSelector = FieldSelector()
        rlOffer.visibility=View.GONE
        if(Common.isCheckNetwork(this@OrderSummuryActivity)){
            callApiOrderSummary()
        }else{
            alertErrorOrValidationDialog(this@OrderSummuryActivity,resources.getString(R.string.no_internet))
        }
        edAddress.setOnClickListener {
            getLocation()
        }
        cvPickup.setOnClickListener {
            select_Delivery=2
            cvPickup.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            cvDelivery.setCardBackgroundColor(resources.getColor(R.color.white))
            cvDeliveryAddress.visibility=View.GONE
           if(tvApply.text.toString().equals("Remove")) {
              tvDiscountOffer.text="-"+discountPer+"%"
              val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*discountPer.toFloat())/100
              val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
              val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
              val mainTotal=ordreTax+total+0.00
              tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+"0.00"
              tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",subtotalCharge)

              tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",mainTotal)
           }else{
              val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100.toFloat()
              tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getOrder_total()!!.toDouble())
              tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderTax)
              tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
              tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+"0.00"
              val totalprice=summaryModel.getOrder_total()!!.toFloat()+orderTax+0.00
              tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",totalprice)
           }
        }
        cvDelivery.setOnClickListener {
            cvDeliveryAddress.visibility=View.VISIBLE
            select_Delivery=1
            cvPickup.setCardBackgroundColor(resources.getColor(R.color.white))
            cvDelivery.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))

            if(tvApply.text.toString().equals("Remove")) {
                tvDiscountOffer.text="-"+discountPer+"%"
                val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*discountPer.toFloat())/100
                val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                val mainTotal=ordreTax+total+summaryModel.getDelivery_charge()!!.toFloat()
                tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getDelivery_charge()!!.toDouble())
                tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",subtotalCharge)
                tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",mainTotal)
            }else{
                val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100.toFloat()
                tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getOrder_total()!!.toDouble())
                tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderTax)
                tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getDelivery_charge()!!.toDouble())
                val totalprice=summaryModel.getOrder_total()!!.toFloat()+orderTax+summaryModel.getDelivery_charge()!!.toFloat()
                tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",totalprice)
            }


        }

        tvProceedToPaymnet.setOnClickListener {
             if(select_Delivery==1){
                 if(edAddress.text.toString()==""){
                     alertErrorOrValidationDialog(
                         this@OrderSummuryActivity,
                         "Por favor ingrese su dirección"
                     )
                 }else if(edBuilding.text.toString() == ""){
                     alertErrorOrValidationDialog(
                         this@OrderSummuryActivity,
                         "Departamento / Nro. Piso es requerido"
                     )
                 }else if(edLandmark.text.toString() == ""){
                     alertErrorOrValidationDialog(
                         this@OrderSummuryActivity,
                         "Referencia es requerido"
                     )
                 }else if(edPinCode.text.toString() == ""){
                     alertErrorOrValidationDialog(
                         this@OrderSummuryActivity,
                         "Código de referencia es requerido"
                     )
                 }else{
                     if(Common.isCheckNetwork(this@OrderSummuryActivity)){
                        val map=HashMap<String,String>()
                        map["pincode"]=edPinCode.text.toString()
                        callApiCheckPinCode(map)
                     }else{
                        alertErrorOrValidationDialog(this@OrderSummuryActivity,resources.getString(R.string.no_internet))
                     }
                 }
             }else if(select_Delivery==2){
                 if(summaryModel.getOrder_total()!!.toDouble()>getStringPref(this@OrderSummuryActivity,isMiniMum)!!.toDouble()&&summaryModel.getOrder_total()!!.toDouble()<getStringPref(this@OrderSummuryActivity,isMaximum)!!.toDouble()){
                    val intent=Intent(this@OrderSummuryActivity,PaymentPayActivity::class.java)
                    val strTotalCharge=tvOrderTotalCharge.text.toString().replace(getStringPref(this@OrderSummuryActivity,isCurrancy)!!,"")
                    val strActuleCharge=strTotalCharge.replace(",","")
                    val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                    intent.putExtra("getAmount",String.format(Locale.US,"%.2f", strActuleCharge.toDouble()))
                    intent.putExtra("getAddress","")
                    intent.putExtra("getTax",summaryModel.getTax())
                    intent.putExtra("getTaxAmount",String.format(Locale.US,"%.2f",orderTax))
                    intent.putExtra("delivery_charge","0.00")
                    intent.putExtra("promocode",tvPromoCodeApply.text.toString())
                    intent.putExtra("discount_pr",discountPer)
                    intent.putExtra("discount_amount",discountAmount)
                    intent.putExtra("order_notes",edNotes.text.toString())
                    intent.putExtra("order_type","2")
                    intent.putExtra("building","")
                    intent.putExtra("landmark","")
                    intent.putExtra("pincode","")
                    intent.putExtra("lat","")
                    intent.putExtra("lon","")
                    startActivity(intent)
                 }else{
                    alertErrorOrValidationDialog(this@OrderSummuryActivity,"Order amount must be between ${getStringPref(this@OrderSummuryActivity,isCurrancy)+getStringPref(this@OrderSummuryActivity,isMiniMum)} and ${getStringPref(this@OrderSummuryActivity,isCurrancy)+getStringPref(this@OrderSummuryActivity,isMaximum)}")
                 }
             }
        }
        ivBack.setOnClickListener {
            finish()
        }

        tvbtnPromocode.setOnClickListener {
            if(Common.isCheckNetwork(this@OrderSummuryActivity)){
              callApiPromocode()
            }else{
              alertErrorOrValidationDialog(this@OrderSummuryActivity,resources.getString(R.string.no_internet))
            }
        }

        tvApply.setOnClickListener {
            if(tvApply.text.toString().equals("Apply")){
                if(!edPromocode.text.toString().equals("")){
                    callApiCheckPromocode()
                }
            }else if(tvApply.text.toString().equals("Remove")) {
                tvPromoCodeApply.text=""
                tvDiscountOffer.text=""
                edPromocode.setText("")
                tvApply.text="Apply"
                rlOffer.visibility=View.GONE
                if(select_Delivery==1){
                    val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                    tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getOrder_total()!!.toDouble())
                    tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderTax)
                    tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                    tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getDelivery_charge()!!.toDouble())
                    val totalprice=summaryModel.getOrder_total()!!.toDouble()+orderTax+summaryModel.getDelivery_charge()!!.toDouble()
                    tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",totalprice)
                    discountPer="0"
                    discountAmount="0.00"
                }else{
                    val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                    tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getOrder_total()!!.toDouble())
                    tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderTax)
                    tvTitleTex.text="Tax (${summaryModel.getTax()}%)"
                    tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",0.00)
                    val totalprice=summaryModel.getOrder_total()!!.toDouble()+orderTax+0.00
                    tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",totalprice)
                    discountPer="0"
                    discountAmount="0.00"
                }

            }
        }

    }

    override fun onBackPressed() {
        finish()
    }

    private fun callApiOrderSummary() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@OrderSummuryActivity,userId)!!
        val call = ApiClient.getClient.setSummary(map)
        call.enqueue(object : Callback<RestSummaryResponse> {
            override fun onResponse(
                call: Call<RestSummaryResponse>,
                response: Response<RestSummaryResponse>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: RestSummaryResponse = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            rvOrderItemFood.visibility = View.VISIBLE
                            val foodCategoryList = restResponce.getData()
                            val summary = restResponce.getSummery()
                            setFoodCategoryAdaptor(foodCategoryList,summary)
                        } else {
                            rvOrderItemFood.visibility = View.GONE
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        rvOrderItemFood.visibility = View.GONE
                    }
                }else{
                    val error=JSONObject(response.errorBody()!!.string())
                    val status=error.getInt("status")
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(this@OrderSummuryActivity,error.getString("message"))
                }
            }

            override fun onFailure(call: Call<RestSummaryResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiPromocode() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@OrderSummuryActivity,userId)!!
        val call = ApiClient.getClient.getPromoCodeList()
        call.enqueue(object : Callback<ListResponse<PromocodeModel>> {
            override fun onResponse(
                call: Call<ListResponse<PromocodeModel>>,
                response: Response<ListResponse<PromocodeModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: ListResponse<PromocodeModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        if (restResponce.getData().size > 0) {
                            promocodeList = restResponce.getData()
                            openDialogPromocode()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ListResponse<PromocodeModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun callApiCheckPromocode() {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@OrderSummuryActivity,userId)!!
        map["offer_code"] = edPromocode.text.toString()
        val call = ApiClient.getClient.setApplyPromocode(map)
        call.enqueue(object : Callback<RestResponse<GetPromocodeModel>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<RestResponse<GetPromocodeModel>>,
                response: Response<RestResponse<GetPromocodeModel>>
            ) {
                if (response.code() == 200) {
                    dismissLoadingProgress()
                    val restResponce: RestResponse<GetPromocodeModel> = response.body()!!
                    if (restResponce.getStatus().equals("1")) {
                        rlOffer.visibility=View.VISIBLE
                        tvDiscountOffer.text="-"+restResponce.getData()!!.getOffer_amount()+"%"
                        tvPromoCodeApply.text=restResponce.getData()!!.getOffer_code()
                        tvApply.text="Remove"
                        if(select_Delivery==1){
                            val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*restResponce.getData()!!.getOffer_amount()!!.toFloat())/100
                            val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                            val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                            val mainTotal=ordreTax+total+summaryModel.getDelivery_charge()!!.toFloat()
                            tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",subtotalCharge)
                            tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",mainTotal)
                            discountAmount=subtotalCharge.toString()
                            discountPer=restResponce.getData()!!.getOffer_amount()!!
                        }else{
                            val subtotalCharge=(summaryModel.getOrder_total()!!.toFloat()*restResponce.getData()!!.getOffer_amount()!!.toFloat())/100
                            val total=summaryModel.getOrder_total()!!.toFloat()-subtotalCharge
                            val ordreTax=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                            val mainTotal=ordreTax+total+0.00
                            tvDiscountOffer.text="-"+getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",subtotalCharge)
                            tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",mainTotal)
                            discountAmount=subtotalCharge.toString()
                            discountPer=restResponce.getData()!!.getOffer_amount()!!
                        }
                    } else if (restResponce.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        edPromocode.setText("")
                        rlOffer.visibility=View.GONE
                        tvApply.text="Apply"
                        alertErrorOrValidationDialog(
                            this@OrderSummuryActivity,
                            restResponce.getMessage()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<GetPromocodeModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }


        })
    }

    @SuppressLint("SetTextI18n")
    private fun setFoodCategoryAdaptor(foodCategoryList: ArrayList<OrderSummaryModel>, summary: SummaryModel?) {
        if(foodCategoryList.size>0){
            setFoodCategoryAdaptor(foodCategoryList)
        }
        summaryModel=summary!!
        val orderTax:Float=(summary.getOrder_total()!!.toFloat()*summary.getTax()!!.toFloat())/100.toFloat()
        tvOrderTotalPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summary.getOrder_total()!!.toDouble())
        tvOrderTaxPrice.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderTax)
        tvTitleTex.text="Tax (${summary.getTax()}%)"
        tvOrderDeliveryCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",summaryModel.getDelivery_charge()!!.toDouble())
        val totalprice=summary.getOrder_total()!!.toFloat()+orderTax+summary.getDelivery_charge()!!.toFloat()
        tvOrderTotalCharge.text=getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",totalprice)
    }

    fun setFoodCategoryAdaptor(orderHistoryList: ArrayList<OrderSummaryModel>) {
        val orderHistoryAdapter = object : BaseAdaptor<OrderSummaryModel>(this@OrderSummuryActivity, orderHistoryList) {
                @SuppressLint("SetTextI18n", "NewApi", "UseCompatLoadingForDrawables")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderSummaryModel,
                    position: Int
                ) {
                    val tvOrderFoodName: TextView = holder!!.itemView.findViewById(R.id.tvFoodName)
                    val ivFoodItem: ImageView = holder.itemView.findViewById(R.id.ivFoodCart)
                    val tvPrice: TextView = holder.itemView.findViewById(R.id.tvPrice)
                    val tvQtyNumber: TextView = holder.itemView.findViewById(R.id.tvQtyPrice)
                    val tvNotes: TextView = holder.itemView.findViewById(R.id.tvNotes)
                    val tvAddons: TextView = holder.itemView.findViewById(R.id.tvAddons)

                    Glide.with(this@OrderSummuryActivity).load(orderHistoryList.get(position).getItemimage().getImage())
                        .placeholder(resources.getDrawable(R.drawable.placeholder)).centerCrop()
                        .into(ivFoodItem)
                    tvOrderFoodName.text = orderHistoryList.get(position).getItem_name()
                    tvPrice.text = getStringPref(this@OrderSummuryActivity,isCurrancy)+String.format(Locale.US,"%,.2f",orderHistoryList.get(position).getTotal_price()!!.toDouble())
                    tvQtyNumber.text ="Cantidad : ${orderHistoryList.get(position).getQty()}"

                    if(orderHistoryList.get(position).getAddons().size>0){
                        tvAddons.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                    }else{
                        tvAddons.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.gray))
                    }
                    if(orderHistoryList.get(position).getItem_notes()==null){
                       tvNotes.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.gray))
                    }else{
                       tvNotes.backgroundTintList=ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                    }

                    holder.itemView.tvAddons.setOnClickListener {
                      if(orderHistoryList.get(position).getAddons().size>0){
                         Common.openDialogSelectedAddons(this@OrderSummuryActivity,orderHistoryList.get(position).getAddons())
                      }
                    }

                    holder.itemView.tvNotes.setOnClickListener {
                        if(orderHistoryList.get(position).getItem_notes()!=null){
                            Common.alertNotesDialog(this@OrderSummuryActivity,orderHistoryList.get(position).getItem_notes())
                        }
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_orderitemsummary
                }

                override fun setNoDataView(): TextView? {
                    return null
                }
            }
        rvOrderItemFood.adapter = orderHistoryAdapter
        rvOrderItemFood.layoutManager = LinearLayoutManager(this@OrderSummuryActivity)
        rvOrderItemFood.itemAnimator = DefaultItemAnimator()
        rvOrderItemFood.isNestedScrollingEnabled = true
    }

    fun openDialogPromocode() {
        val dialog: Dialog = Dialog(this@OrderSummuryActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.windowAnimations = R.style.DialogAnimation
        dialog.window!!.attributes = lp
        dialog.setContentView(R.layout.dlg_procode)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivCancel = dialog.findViewById<ImageView>(R.id.ivCancel)
        val rvPromocode = dialog.findViewById<RecyclerView>(R.id.rvPromoCode)
        val tvNoDataFound = dialog.findViewById<TextView>(R.id.tvNoDataFound)
        if(promocodeList!!.size>0){
            rvPromocode.visibility=View.VISIBLE
            tvNoDataFound.visibility=View.GONE
            setPromocodeAdaptor(promocodeList!!,rvPromocode,dialog)
        }else{
            rvPromocode.visibility=View.GONE
            tvNoDataFound.visibility=View.VISIBLE
        }

        ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun setPromocodeAdaptor(
        promocodeList: ArrayList<PromocodeModel>,
        rvPromocode: RecyclerView,
        dialog: Dialog
    ) {
        val orderHistoryAdapter = object : BaseAdaptor<PromocodeModel>(this@OrderSummuryActivity, promocodeList) {
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: PromocodeModel,
                position: Int
            ) {
                val tvTitleOrderNumber: TextView = holder!!.itemView.findViewById(R.id.tvTitleOrderNumber)
                val tvPromocode: TextView = holder.itemView.findViewById(R.id.tvPromocode)
                val tvPromocodeDescription: TextView = holder.itemView.findViewById(R.id.tvPromocodeDescription)
                val tvCopyCode: TextView = holder.itemView.findViewById(R.id.tvCopyCode)

                tvTitleOrderNumber.text = promocodeList.get(position).getOffer_name()
                tvPromocode.text =promocodeList.get(position).getOffer_code()
                tvPromocodeDescription.text =promocodeList.get(position).getDescription()

                tvCopyCode.setOnClickListener {
                    dialog.dismiss()
                    promocodePrice=promocodeList.get(position).getOffer_amount()!!.toFloat()
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.text = promocodeList.get(position).getOffer_code()
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_promocode
            }

            override fun setNoDataView(): TextView? {
                return null
            }
        }
        rvPromocode.adapter = orderHistoryAdapter
        rvPromocode.layoutManager = LinearLayoutManager(this@OrderSummuryActivity)
        rvPromocode.itemAnimator = DefaultItemAnimator()
        rvPromocode.isNestedScrollingEnabled = true
    }

    private fun getLocation() {
        val autocompleteIntent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fieldSelector!!.allFields
        ).build(this@OrderSummuryActivity)
        startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                edAddress.text=place.getAddress()
                val latLng: String = place.latLng.toString()
                val tempArray = latLng.substring(latLng.indexOf("(") + 1, latLng.lastIndexOf(")")).split(",")
                        .toTypedArray()
                lat = tempArray[1].toDouble()
                lon = tempArray[0].toDouble()
                /*val geocoder = Geocoder(this@OrderSummuryActivity, Locale.getDefault())
                try {
                    val addressList: List<Address>? =geocoder.getFromLocation(lat,lon, 1)
                    if (addressList != null && addressList.size> 0) {
                        country_code = addressList[0].countryCode

                      //  Common.getLog("getKey",geocoder.get(lat,lon, 1).toString())
                        if(addressList[0].postalCode!=null){
                            postal_code = addressList[0].postalCode
                        }

                        if(addressList[0].locality!=null&&addressList[0].subLocality!=null){
                            city = addressList[0].subAdminArea
                        }else if(addressList[0].locality!=null&&addressList[0].subLocality==null){
                            city = addressList[0].locality
                        }else if(addressList[0].locality==null&&addressList[0].subLocality!=null){
                            city = addressList[0].subLocality
                        }else if(addressList[0].locality==null&&addressList[0].subLocality==null){
                            city=""
                        }

                        if(addressList[0].adminArea!=null&&addressList[0].subAdminArea!=null){
                            state=addressList[0].adminArea
                        }else if(addressList[0].adminArea!=null&&addressList[0].subAdminArea==null){
                            state=addressList[0].adminArea
                        }else if(addressList[0].adminArea==null&&addressList[0].subAdminArea!=null){
                            state=addressList[0].subAdminArea
                        }else if(addressList[0].adminArea==null&&addressList[0].subAdminArea==null){
                            state=""
                        }
                    }
                } catch (e: IOException) {

                }*/
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
               val status: Status = Autocomplete.getStatusFromIntent(data!!)
               Common.showErrorFullMsg(this@OrderSummuryActivity,"invalid google map key")
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
               Common.getLog("Nice", " RESULT_CANCELED : AutoComplete Places")
            }
        }
    }

    private fun callApiCheckPinCode(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@OrderSummuryActivity)
        val call = ApiClient.getClient.setCheckPinCode(hasmap)
        call.enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if(response.code()==200){
                    Common.dismissLoadingProgress()
                    val singleResponse: SingleResponse = response.body()!!
                    if (singleResponse.getStatus()=="0") {
                        edPinCode.setText("")
                        Common.showErrorFullMsg(
                            this@OrderSummuryActivity,
                            singleResponse.getMessage()!!
                        )
                    }else if (singleResponse.getStatus()=="1") {
                       if(summaryModel.getOrder_total()!!.toDouble()>getStringPref(this@OrderSummuryActivity,isMiniMum)!!.toDouble()&&summaryModel.getOrder_total()!!.toDouble()<getStringPref(this@OrderSummuryActivity,isMaximum)!!.toDouble()){
                          val intent=Intent(this@OrderSummuryActivity,PaymentPayActivity::class.java)
                          val strTotalCharge=tvOrderTotalCharge.text.toString().replace(getStringPref(this@OrderSummuryActivity,isCurrancy)!!,"")
                          val strActuleCharge=strTotalCharge.replace(",","")
                          val orderTax:Float=(summaryModel.getOrder_total()!!.toFloat()*summaryModel.getTax()!!.toFloat())/100
                          intent.putExtra("getAmount",String.format(Locale.US,"%.2f", strActuleCharge.toDouble()))
                          intent.putExtra("getAddress",edAddress.text.toString())
                          intent.putExtra("getTax",summaryModel.getTax())
                          intent.putExtra("getTaxAmount",String.format(Locale.US,"%.2f",orderTax))
                          intent.putExtra("delivery_charge",String.format(Locale.US,"%.2f",summaryModel.getDelivery_charge()!!.toDouble()))
                          intent.putExtra("promocode",tvPromoCodeApply.text.toString())
                          intent.putExtra("discount_pr",discountPer)
                          intent.putExtra("discount_amount",discountAmount)
                          intent.putExtra("order_notes",edNotes.text.toString())
                          intent.putExtra("lat",lat.toString())
                          intent.putExtra("lon",lon.toString())
                          intent.putExtra("order_type","1")
                          intent.putExtra("building",edBuilding.text.toString())
                          intent.putExtra("landmark",edLandmark.text.toString())
                          intent.putExtra("pincode",edPinCode.text.toString())
                          startActivity(intent)
                       }else{
                           alertErrorOrValidationDialog(this@OrderSummuryActivity,"Order amount must be between ${getStringPref(this@OrderSummuryActivity,isCurrancy)+getStringPref(this@OrderSummuryActivity,isMiniMum)} and ${getStringPref(this@OrderSummuryActivity,isCurrancy)+getStringPref(this@OrderSummuryActivity,isMaximum)}")
                       }

                    }
                } else  {
                    val error= JSONObject(response.errorBody()!!.string())
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        this@OrderSummuryActivity,
                        error.getString("message")
                    )
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@OrderSummuryActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }


}