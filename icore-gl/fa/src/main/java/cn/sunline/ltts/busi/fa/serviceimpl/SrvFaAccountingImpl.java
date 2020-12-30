package cn.sunline.ltts.busi.fa.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.accounting.FaSetAccountApply;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaOffBanlceAccountingIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaQuerySetAccountsOut;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaaOutAccountTable;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDetail;

/**
 * 记账类服务
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SrvFaAccountingImpl", longname = "记账类服务", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaAccountingImpl implements cn.sunline.ltts.gl.fa.servicetype.SrvFaAccounting {
	/**
	 * 申请套号
	 */
	public cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsMain applySetAccountsNo(GlEnumType.E_YESORNO subject_accounting_ind, String remark) {
		return FaSetAccountApply.applySetAccountsNo(remark, subject_accounting_ind);
	}

	

	public cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsMain bookSetAccount(String set_account_no) {

		return FaSetAccountApply.bookSetAccount(set_account_no);
	}

	public void delSetAccountDetail(final cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDatailDelIn detailDelIn) {
		FaSetAccountApply.delSetAccountDetail(detailDelIn);
	}

	public void invalidSetAcccounts(final cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDatailDelIn setAccountsInvaidIn) {
		FaSetAccountApply.invalidSetAcccounts(setAccountsInvaidIn.getSet_account_no(), setAccountsInvaidIn.getRecdver());

	}

	@Override
	public FaQuerySetAccountsOut querySetAccountsDeitail(String set_account_no) {

		return FaSetAccountApply.querySetAccountsDeitail(set_account_no);

	}

    @Override
    public FaaOutAccountTable bookOffBalanceAccounting(FaOffBanlceAccountingIn offBalanceIn) {
        return FaAccounting.bookOffBalanceAccounting(offBalanceIn);
    }

   
 
    //套账查询
    public void querySetAccountsDeitails( final cn.sunline.ltts.gl.fa.servicetype.SrvFaAccounting.querySetAccountsDeitails.Input input,  final cn.sunline.ltts.gl.fa.servicetype.SrvFaAccounting.querySetAccountsDeitails.Output output){
    	FaSetAccountApply.querySetAccountsDeitails(input,output);
    }


    //套账明细录入
    public void addSetAccountsDetails( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDetail> setDetailIn){
        //平衡性校验
        BigDecimal total=BigDecimal.ZERO;
        int debittotal=0,credittotal=0;
        if(CommUtil.isNull(setDetailIn) || setDetailIn.size() == 0) {
        	throw GlError.GL.E0215(); 
        }
        for(FaSetAccountsDetail faSetAccountsDetail : setDetailIn){
            //明细录入
            FaSetAccountApply.addSetAccountsDetails(faSetAccountsDetail,faSetAccountsDetail.getSet_account_no());
            //明细录入总金额
            total=total.add(FaSetAccountApply.checkBanlance(faSetAccountsDetail));
            //累计借贷方笔数
            switch (faSetAccountsDetail.getDebit_credit()) {
            case DEBIT:
                debittotal++;
                break;
            case CREDIT:
                credittotal++;
                break;
            default:
                break;
            }
        }
        //借贷方笔数不能同时大于1
        if(debittotal>1&&credittotal>1){
            //不允许多借多贷
            throw GlError.GL.E0202(debittotal,credittotal);
        }
        //判断借贷方差额是否等于0
        if(CommUtil.compare(total, BigDecimal.ZERO)!=0){
            //借贷不平衡
            throw GlError.GL.E0052();//jym del 因为支持删除和修改单比账务，所以无法持续保证平衡性，所以先注释平衡性校验，在入账时候有校验平衡性；后期需要优化
        }
       
    }

    // 明细修改
    public void modifySetAccountDetail( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDetail> setAccountDetails){
        
      //平衡新校验
        BigDecimal total = BigDecimal.ZERO;
        int debitTotal = 0;
        int creditTotal = 0;
        for(FaSetAccountsDetail setAccountDetail : setAccountDetails){
            //明细修改
            FaSetAccountApply.modfiySetAccountsDeitail(setAccountDetail);
            //明细录入总金额
            total = total.add(FaSetAccountApply.checkBanlance(setAccountDetail));
            //累计借贷方笔数
            switch(setAccountDetail.getDebit_credit()){
                case DEBIT:
                    debitTotal++;
                    break;
                case CREDIT:
                    creditTotal++;
                    break;
                default:
                    break;
            }
           
        }
        //借贷方明细不能同时大于1
        if(debitTotal>1&&creditTotal>1){
            //不允许多借多贷
            throw GlError.GL.E0202(debitTotal,creditTotal);
        }
        //判断借贷方差额是否等于0
        if(CommUtil.compare(total, BigDecimal.ZERO)!=0){
            //借贷不平衡
            throw GlError.GL.E0052();//jym del 因为支持删除和修改单比账务，所以无法持续保证平衡性，所以先注释平衡性校验，在入账时候有校验平衡性；后期需要优化
        }
    }


   
    


    
   

    



   



 



   

   

}
