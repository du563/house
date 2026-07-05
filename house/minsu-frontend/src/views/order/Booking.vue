<template>
  <div class="booking-container">
    <div class="container">
      <el-card class="booking-card">
        <div slot="header">
          <span>预订房源</span>
        </div>
        
        <el-row :gutter="40">
          <!-- 房源信息 -->
          <el-col :span="12">
            <div class="house-info" v-if="house">
              <img :src="house.images ? house.images.split(',')[0] : '/images/default.jpg'" loading="lazy" class="house-image" alt="">
              <h3>{{ house.name }}</h3>
              <p><i class="el-icon-location"></i> {{ house.area }} · {{ house.type }}</p>
              <p class="price">¥{{ house.price }} /晚</p>
            </div>
          </el-col>
          
          <!-- 预订表单 -->
          <el-col :span="12">
            <el-form ref="bookingForm" :model="form" :rules="rules" label-width="100px">
              <el-form-item label="入住日期" prop="checkInDate">
                <el-date-picker 
                  v-model="form.checkInDate" 
                  type="date" 
                  placeholder="选择入住日期"
                  :picker-options="checkInOptions"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
              <el-form-item label="离店日期" prop="checkOutDate">
                <el-date-picker 
                  v-model="form.checkOutDate" 
                  type="date" 
                  placeholder="选择离店日期"
                  :picker-options="checkOutOptions"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
              <el-form-item label="入住人数" prop="guests">
                <el-input-number v-model="form.guests" :min="1" :max="house ? house.capacity : 10"></el-input-number>
              </el-form-item>
              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="form.contactName" placeholder="请输入联系人姓名"></el-input>
              </el-form-item>
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话"></el-input>
              </el-form-item>
              <el-form-item label="备注">
                <el-input type="textarea" v-model="form.remark" placeholder="特殊需求可在此备注"></el-input>
              </el-form-item>
              
              <!-- 价格计算 -->
              <div class="price-summary" v-if="totalDays > 0">
                <p>入住 {{ totalDays }} 晚</p>
                <p class="total-price">总价：<span>¥{{ totalPrice }}</span></p>
              </div>
              
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="handleSubmit">确认预订</el-button>
                <el-button @click="$router.back()">返回</el-button>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getHouseDetail } from '@/api/house'
import { createOrder } from '@/api/order'

export default {
  name: 'Booking',
  data() {
    return {
      house: null,
      form: {
        checkInDate: null,
        checkOutDate: null,
        guests: 1,
        contactName: '',
        contactPhone: '',
        remark: ''
      },
      rules: {
        checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
        checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }],
        contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
        contactPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },
      loading: false,
      checkInOptions: {
        disabledDate: (time) => {
          return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
        }
      }
    }
  },
  computed: {
    checkOutOptions() {
      return {
        disabledDate: (time) => {
          if (this.form.checkInDate) {
            return time.getTime() <= new Date(this.form.checkInDate).getTime()
          }
          return time.getTime() < Date.now()
        }
      }
    },
    totalDays() {
      if (this.form.checkInDate && this.form.checkOutDate) {
        const diff = new Date(this.form.checkOutDate).getTime() - new Date(this.form.checkInDate).getTime()
        return Math.ceil(diff / (24 * 60 * 60 * 1000))
      }
      return 0
    },
    totalPrice() {
      if (this.house && this.totalDays > 0) {
        return (this.house.price * this.totalDays).toFixed(2)
      }
      return '0.00'
    }
  },
  created() {
    this.loadHouse()
  },
  methods: {
    async loadHouse() {
      try {
        const res = await getHouseDetail(this.$route.params.houseId)
        this.house = res.data
      } catch (e) {
        this.$message.error('房源信息加载失败')
        this.$router.back()
      }
    },
    handleSubmit() {
      this.$refs.bookingForm.validate(async valid => {
        if (!valid) return
        if (this.totalDays <= 0) {
          this.$message.warning('请选择正确的入住和离店日期')
          return
        }
        
        this.loading = true
        try {
          const formatDate = (date) => {
            const d = new Date(date)
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
          }
          
          const data = {
            houseId: this.house.id,
            checkInDate: formatDate(this.form.checkInDate),
            checkOutDate: formatDate(this.form.checkOutDate),
            guests: this.form.guests,
            contactName: this.form.contactName,
            contactPhone: this.form.contactPhone,
            remark: this.form.remark
          }
          
          const res = await createOrder(data)
          this.$message.success('预订成功')
          this.$router.push(`/order/${res.data.id}`)
        } catch (e) {
          console.error(e)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.booking-container {
  padding: 20px 0;
}

.house-info {
  text-align: center;
}

.house-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 15px;
}

.house-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.house-info p {
  color: #666;
  margin-bottom: 5px;
}

.house-info .price {
  font-size: 24px;
  color: #D97706;
  font-weight: bold;
  margin-top: 15px;
}

.price-summary {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-summary .total-price {
  font-size: 18px;
  margin-top: 10px;
}

.price-summary .total-price span {
  font-size: 28px;
  color: #D97706;
  font-weight: bold;
}
</style>
