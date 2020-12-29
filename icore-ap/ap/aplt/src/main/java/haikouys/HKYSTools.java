package haikouys;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.SnError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USERST;
import cn.sunline.ltts.busi.sys.type.SnBaseEnumType.E_CERTTP;
import cn.sunline.ltts.busi.sys.type.SnBaseEnumType.E_TELLST;

public class HKYSTools {
/**
 * 将系统内证件类型转换为神码核心证件类型
 * author liusikai
 * date 20190316   10:58:21
 * @param idtftp
 * @return
 */
    public static E_CERTTP idtftpTransfer(E_IDTFTP idtftp) {
        E_CERTTP certtp = null;
        if (CommUtil.isNull(idtftp)) {
            return certtp;
        }
        switch (idtftp) {
        case SFZ://身份证
            certtp = E_CERTTP.SFZ;
            break;
            
        case HKB://户口簿
            certtp = E_CERTTP.HKB;
            break;
            
        case HZ://护照 
            certtp = E_CERTTP.HZ;       
            break;
            
        case JGZ://军官证 
            certtp = E_CERTTP.JGZ;
            break;
            
        case SBZ:// 士兵证 
            certtp = E_CERTTP.SBZ; 
            break;
            
        case GA:// 港澳居民来往内地通行证
            certtp = E_CERTTP.GA; 
            break;
            
        case TW://台湾同胞来往内地通行证
            certtp = E_CERTTP.TW;   
            break;
            
        case LS://临时身份证 
            certtp = E_CERTTP.LSF; 
            break;
            
        case QGR://其他证件
            certtp = E_CERTTP.X; 
            break;
            
        case YYZ://营业执照
            certtp = E_CERTTP.Y; 
            break;
            
        case PWP:// 审批件 
            certtp = E_CERTTP.P; 
            break;
            
        case JRX://金融许可证 
            certtp = E_CERTTP.J; 
            break;
            
        case ZZJ:// 组织机构代码证 
            certtp = E_CERTTP.Z; 
            break;
            
        case F://移植用
            certtp = E_CERTTP.F; 
            break;
            
        case HXZ://回乡证 
            certtp = E_CERTTP.HXZ; 
            break;

        default:
            throw SnError.SnAcct.E0003("核心暂未做该证件映射，请即将枚举值提供给核心确认。");
        }
        
        return certtp;
    }
    
    /**
     * 将神码核心证件类型转换为系统内证件类型
     * author liusikai
     * @param certtp
     * @return
     */
    public static E_IDTFTP certtpTransfer(E_CERTTP certtp) {
        E_IDTFTP idtftp = null;
        if (CommUtil.isNull(certtp)) {
            return idtftp;
        }
        switch (certtp) {
        case SFZ://身份证
            idtftp = E_IDTFTP.SFZ;
            break;
            
        case HKB://户口簿
            idtftp = E_IDTFTP.HKB;
            break;
            
        case HZ://护照 
            idtftp = E_IDTFTP.HZ;       
            break;
            
        case JGZ://军官证 
            idtftp = E_IDTFTP.JGZ;
            break;
            
        case SBZ:// 士兵证 
            idtftp = E_IDTFTP.SBZ; 
            break;
            
        case GA:// 港澳居民来往内地通行证
            idtftp = E_IDTFTP.GA; 
            break;
            
        case TW://台湾同胞来往内地通行证
            idtftp = E_IDTFTP.TW;   
            break;
            
        case LSF://临时身份证 
            idtftp = E_IDTFTP.LS; 
            break;
            
        case X://其他证件
            idtftp = E_IDTFTP.QGR; 
            break;
            
        case Y://营业执照
            idtftp = E_IDTFTP.YYZ; 
            break;
            
        case P:// 审批件 
            idtftp = E_IDTFTP.PWP; 
            break;
            
        case J://金融许可证 
            idtftp = E_IDTFTP.JRX; 
            break;
            
        case Z:// 组织机构代码证 
            idtftp = E_IDTFTP.ZZJ; 
            break;
            
        case F://移植用
            idtftp = E_IDTFTP.F; 
            break;
            
        case HXZ://回乡证 
            idtftp = E_IDTFTP.HXZ; 
            break;

        default:
            throw SnError.SnAcct.E0003("核心暂未做该证件映射，请即将枚举值提供给核心确认。");
        }
        
        return idtftp;
    }
    
    /**
     * 将神码核心柜员状态与新核心柜员状态进行映射
     * author liusikai
     * @param certtp
     * @return
     */
    public static E_TELLST tellerStatusTransfer(E_USERST userst) {
        
        E_TELLST tellst = null;
        //没有输入时直接返回空
        if (CommUtil.isNull(userst)) {
            return  tellst;
        }
        
        if (userst == E_USERST.START) {
            tellst = E_TELLST.EFFECT;   //柜员为启用状态时对应有效状态         
        }else {
            tellst = E_TELLST.USENESS;  //其他状态为无效
        }
        return tellst;
        
    }
}
