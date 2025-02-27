package com.example.paytabs_demo_store_android.bag.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.paytabs_demo_store_android.bag.view.adapter.BagProductsAdapter
import com.example.paytabs_demo_store_android.bag.viewmodel.BagViewModel
import com.example.paytabs_demo_store_android.database.dao.BagDao
import com.example.paytabs_demo_store_android.database.dao.OrdersDao
import com.example.paytabs_demo_store_android.database.enities.OrdersEntity
import com.example.paytabs_demo_store_android.databinding.FragmentBagBinding
import com.example.paytabs_demo_store_android.products.model.response.Product
import com.payment.paymentsdk.PaymentSdkActivity
import com.payment.paymentsdk.PaymentSdkConfigBuilder
import com.payment.paymentsdk.integrationmodels.*
import com.payment.paymentsdk.sharedclasses.interfaces.CallbackPaymentInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BagFragment : Fragment() , CallbackPaymentInterface {

    private var token: String? = null
    private var transRef: String? = null

    @Inject
    lateinit var bagDao: BagDao
    @Inject
    lateinit var ordersDao: OrdersDao

    var totalPrice: Double = 0.0
    private val viewModel: BagViewModel by viewModels()
    private lateinit var binding: FragmentBagBinding
    private val adapter = BagProductsAdapter({deleteProduct(it)} ,
        { productId -> increaseItemCount(productId) } ,
        { productId -> decreaseItemCount(productId) } )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.getBagProducts()
        binding = FragmentBagBinding.inflate(inflater)
        initRV()
        observeViewModel()
        onClickActions()
        return binding.root
    }

    private fun onClickActions() {
        binding.btnCheckOut.setOnClickListener {
            val configData = generatePaytabsConfigurationDetails()
            PaymentSdkActivity.startCardPayment(requireActivity(), configData, this)
        }
    }



    private fun observeViewModel() {
        viewModel.bagProducts.observe(viewLifecycleOwner, {
            adapter.setProducts(it)
            totalPrice=adapter.getTotalPrice(it).toDouble()
            binding.tvTotalPrice.text = "Total price ${totalPrice.toString()}$"
        })


    }

    private fun initRV() {
        binding.rvBag.adapter=adapter
    }

    private fun deleteProduct(product: Product) {
        viewModel.deleteBagProduct(product)
    }

    private fun increaseItemCount( productId:Int) {
        viewModel.increaseCount(productId)
    }

    private fun decreaseItemCount(productId:Int) {
        viewModel.decreaseCount(productId)
    }




    private fun generatePaytabsConfigurationDetails(): PaymentSdkConfigurationDetails {

        val configData = PaymentSdkConfigBuilder(
            "74607",
            "S2JNW22GGM-J2BJLKR296-NDW6MR9MHJ",
            "CTKMGB-P26R6M-26BKRM-9PKNPG",
            totalPrice,
            "EGP"
        )
            .setCartDescription("cartDesc")
            .setLanguageCode(PaymentSdkLanguageCode.EN)
            .setMerchantCountryCode("eg")
            .setTransactionType(PaymentSdkTransactionType.SALE)
            .setTransactionClass(PaymentSdkTransactionClass.ECOM)
            .setCartId("12")
            .setShippingData(PaymentSdkShippingDetails())
            .showBillingInfo(true)
            .showShippingInfo(true)
            .setScreenTitle("Pay with Card")


        return configData.build()
    }


    override fun onError(error: PaymentSdkError) {
        Toast.makeText(requireActivity(), "${error.msg}", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentCancel() {
        Toast.makeText(requireActivity(), "onPaymentCancel", Toast.LENGTH_SHORT).show()

    }

    override fun onPaymentFinish(paymentSdkTransactionDetails: PaymentSdkTransactionDetails) {
        token = paymentSdkTransactionDetails.token
        transRef = paymentSdkTransactionDetails.transactionReference
        val currentTime: Date = Calendar.getInstance().getTime()
        val order = OrdersEntity("John" , totalPrice ,currentTime.toString() , "Authorized")
        Toast.makeText(
            requireActivity(),
            "${paymentSdkTransactionDetails.paymentResult?.responseMessage ?: "PaymentFinish"}",
            Toast.LENGTH_SHORT
        ).show()
        viewModel.deleteAllBagProducts()
        CoroutineScope(Dispatchers.IO).launch {
            ordersDao.addOrder(order)
        }
    }


}