import React from 'react'
import { Modal, Form, DatePicker, Input, message } from 'antd'
import type { Contract, ContractRenewParams } from '@/types/contract'

interface ContractRenewModalProps {
  visible: boolean
  contract: Contract | null
  onSubmit: (values: ContractRenewParams) => void
  onCancel: () => void
}

const ContractRenewModal: React.FC<ContractRenewModalProps> = ({
  visible,
  contract,
  onSubmit,
  onCancel
}) => {
  const [form] = Form.useForm()

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      onSubmit(values)
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  }

  return (
    <Modal
      title="续签合同"
      open={visible}
      onCancel={onCancel}
      onOk={handleSubmit}
      width={600}
      destroyOnClose
    >
      {contract && (
        <div style={{ marginBottom: 16, padding: 16, backgroundColor: '#f5f5f5', borderRadius: 4 }}>
          <div><strong>合同编号：</strong>{contract.contractNo}</div>
          <div><strong>员工姓名：</strong>{contract.employeeName}</div>
          <div><strong>当前结束日期：</strong>{contract.endDate}</div>
          <div><strong>已续签次数：</strong>{contract.renewCount}</div>
        </div>
      )}
      
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          newSignDate: new Date().toISOString().split('T')[0]
        }}
      >
        <Form.Item
          name="newEndDate"
          label="新结束日期"
          rules={[
            { required: true, message: '请选择新结束日期' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || !contract) {
                  return Promise.resolve()
                }
                const newEndDate = new Date(value)
                const currentEndDate = new Date(contract.endDate)
                if (newEndDate <= currentEndDate) {
                  return Promise.reject(new Error('新结束日期必须晚于当前结束日期'))
                }
                return Promise.resolve()
              },
            }),
          ]}
        >
          <DatePicker style={{ width: '100%' }} placeholder="请选择新结束日期" />
        </Form.Item>

        <Form.Item
          name="newSignDate"
          label="新签署日期"
          rules={[{ required: true, message: '请选择新签署日期' }]}
        >
          <DatePicker style={{ width: '100%' }} placeholder="请选择新签署日期" />
        </Form.Item>

        <Form.Item
          name="renewReason"
          label="续签原因"
          rules={[{ required: true, message: '请输入续签原因' }]}
        >
          <Input.TextArea rows={4} placeholder="请输入续签原因" />
        </Form.Item>

        <Form.Item
          name="remark"
          label="备注"
        >
          <Input.TextArea rows={2} placeholder="请输入备注" />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default ContractRenewModal
