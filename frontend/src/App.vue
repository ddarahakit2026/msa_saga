<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import axios from "axios";
import PortOne from "@portone/browser-sdk/v2"

const productList = ref([])
const selected = ref([])
const isPaymentProcessing = ref(false)

const totalPrice = computed(() => {
  return selected.value.reduce((acc, cur) => acc + (cur.price * cur.quantity), 0)
})

// 결제 상태
const paymentStatus = ref({
  status: "",
  message: ""
});

const getproductList = async () => {
  const res = await axios.get('http://localhost:8083/product/list')
  if (res.data) {
    productList.value = (res.data || []).map(p => ({...p, quantity: 1}))
  }
}

const toggleSelected = (product) => {
  const index = selected.value.findIndex(p => p.idx === product.idx)
  if (index > -1) {
    selected.value.splice(index, 1)
  } else {
    selected.value.push(product)
  }
}

const onPayment = async () => {
  if (selected.value.length === 0) return
  if (isPaymentProcessing.value) return

  isPaymentProcessing.value = true
  paymentStatus.value = {status: "", message: ""}

  let ordersIdx = null

  const firstItem = selected.value[0]
  const orderItems = selected.value.map(product => ({
    productIdx: product.idx,
    quantity: product.quantity
  }))
  const productIdxList = selected.value.map(product => product.idx)
  const orderName = selected.value.length === 1
      ? firstItem.name
      : `${firstItem.name} 외 ${selected.value.length - 1}건`

  // 주문 생성
  const createResponse = await axios.post('http://localhost:8081/orders/create', {
        paymentPrice: totalPrice.value,
        ordersItems: orderItems
      }
  )

  ordersIdx = createResponse.data.idx


  const paymentId = Math.floor(Math.random() * 101);
  // 결제창 띄우기
  const payment = await PortOne.requestPayment({
    storeId: "store-a98efd4b-3978-4db3-ac72-59949fba4f1e",
    channelKey: "channel-key-996b09cf-d516-4091-8e08-5882ef3479f8",
    paymentId: "imp_923865ifdg7ig" + paymentId,
    orderName: orderName,
    totalAmount: totalPrice.value,
    currency: 'KRW',
    payMethod: "CARD",
    customData: {ordersIdx, productIdxList}
  }).then((res) => {
    return res;
  }).catch((error) => {
    paymentStatus.value = {status: "FAILED", message: '결제 시도가 실패하였습니다. 잠시 후 다시 시도해주세요.'}
  });

  // 3. 결제 검증 (api-payment 서비스 호출)
  const verifyResponse = await axios.post('http://localhost:8082/payment/verify', {
    paymentId: payment.paymentId,
    ordersIdx: ordersIdx
  })
}

onMounted(async () => {
  await getproductList()
})
</script>

<template>

  <div v-for="product in productList" :key="product.idx">
    <div style="margin-bottom: 10px; display: flex; align-items: center; gap: 10px;">
      <input type="checkbox" :checked="selected.some(p => p.idx === product.idx)"
             @change="toggleSelected(product)">
      <span>ID: {{ product.idx }}</span>
      <span>상품명: {{ product.name }}</span>
      <span>가격: {{ product.price }}원</span>
      <label>
        수량:
        <input type="number" v-model.number="product.quantity" min="1" style="width: 60px;">
      </label>
    </div>
  </div>
  <div style="margin-top: 20px;">
    <strong>총 결제 금액: {{ totalPrice }}원</strong>
  </div>
  <button @click="onPayment" :disabled="selected.length === 0" style="margin-top: 10px;">결제하기</button>

</template>

<style scoped>


</style>
