<template>
    <div class="ivu-layout serviceDetail">
        <serviceDetailCard v-if="serviceDetails.show" :changeDisabled='serviceDetails.dis' :show='serviceDetails.show'></serviceDetailCard>
        <dealDetailCard v-if="dealDetails.show" :changeDisabled='dealDetails.dis' :show='dealDetails.show'></dealDetailCard>
        <feedbackCard></feedbackCard>
    </div>
</template>

<script>
import serviceDetailCard from "./serviceCard/serviceDetailCard.vue";
import dealDetailCard from "./serviceCard/dealDetailCard.vue";
import feedbackCard from "./serviceCard/feedbackCard.vue";
export default {
    data(){
        return{
            serviceDetails:{},
            dealDetails:{}
        }
    },
    created(){
        this.serviceDetails = JSON.parse(this.$route.params.serviceDetails);
        this.dealDetails = JSON.parse(this.$route.params.dealDetails);
    },
    methods:{
        goback(){
            this.$router.go(-1);
        }
    },
    components: {
        serviceDetailCard,
        dealDetailCard,
        feedbackCard
    },
}
</script>

<style lang='less' scoped>
.serviceDetail{
    background:#eee;
    padding: 20px;
    width:80%;
    height:auto;
}
</style>
