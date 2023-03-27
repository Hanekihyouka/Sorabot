package haneki

import com.google.gson.GsonBuilder
import haneki.module.MessageModule
import haneki.module.ModuleLoader
import haneki.module.NeedOperactor
import haneki.module.instance.*
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDABuilder
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.BotConfiguration
//-import xyz.cssxsh.mirai.tool.FixProtocolVersion
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

var cfg_core = Config()
var moduleLoader = ModuleLoader()


suspend fun main() {
    preInit()

    // fffffffffff
    //-FixProtocolVersion.update();

    //乙烯 手机
    //忍冬 ipad
    val bot = BotFactory.newBot(
        //***REMOVED***,//忍冬
        //***REMOVED***
        2877520250,//乙烯
        "***REMOVED***"
    ){
        fileBasedDeviceInfo("device.json")
        autoReconnectOnForceOffline()
        redirectNetworkLogToFile()
        //切换协议
        protocol = BotConfiguration.MiraiProtocol.IPAD
        //protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
        //切换心跳策略
        heartbeatStrategy = BotConfiguration.HeartbeatStrategy.STAT_HB
    }.alsoLogin()

    bot.updateConfig()
    bot.jdaBuilder(bot)
    bot.messageDSL()
    timer(bot)
    bot.join() // 等待 Bot 离线, 避免主线程退出
}

fun Bot.updateConfig(){
    bot.groups.forEach{
        if (!cfg_core.groupConfigList.containsKey(it.id)){//配置里没有该group，生成个空的
            println("[status]没有在配置文件中找到[群:" + it.id + "]的配置项，正在自动生成空项。")
            val groupConfig = GroupConfig()
            groupConfig.group_id = it.id
            cfg_core.groupConfigList.put(it.id,groupConfig)
        }
    }
    saveConfig(File("./config/core.json"))
}

fun Bot.getPermissionLevel(id:Long): Int {
    if (cfg_core.superadmin.contains(id)){
        return 100
    }else if(cfg_core.operator.contains(id)){
        return 10
    }else{
        return 0
    }
}

fun Bot.messageDSL(){

    this.eventChannel.subscribeAlways<MemberJoinRequestEvent> {
        group?.sendMessage(fromNick + "(" + this.fromId + ") 申请加入本群。\n入群申请消息>\n" + message)
        eventChannel.filter {
            it is GroupMessageEvent &&
                    it.group.id == this.groupId &&
                    (getPermissionLevel(it.sender.id)>9||it.sender.permission.level>0) &&
                    (Regex("#(同意|拒绝) ?$fromId").matches(it.message.contentToString()))
        }.subscribeOnce<GroupMessageEvent> {
                if (this.message.contentToString().contains("同意")){
                    accept()
                }else{
                    reject()
                }
        }
    }

    this.eventChannel.subscribeAlways<MemberJoinEvent> {
        if (cfg_core.groupConfigList.get(group.id)?.isGreet == true){//入群欢迎
            group.sendMessage(At(member) + "\u202D" + cfg_core.groupConfigList.get(group.id)!!.greet)
        }
    }

    this.eventChannel.subscribeMessages {
        // Regex("(.|\r|\n)*:.*?:(.|\r|\n).*")matching{ System.out.println("[RegexTest] Done!") }

        Regex("(?i).?.?help")matching{
            subject.sendMessage("SoraBot使用指南>\n" +
                    "http://100oj.com/zh/%E5%B7%A5%E5%85%B7/SoraBot%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97")
        }
        /**
         * ess模块的一部分，涉及核心的内容，直接写在这里
         * 其实是在偷懒(
         * */
        Regex("(?i)##Ess(ential(s(X)?)?)?.*")matching{
            val params = message.contentToString().split(" ")
            val messageChainBuilder = MessageChainBuilder()
            when(params[1]){
                "module","-m" ->{
                    when(params[2]){
                        "enable","-on" ->{
                            val module2bEn = params[3]//moduleName
                            var group2bSet = 0L
                            when(message.source.kind){MessageSourceKind.GROUP ->{
                                group2bSet = message.source.targetId
                                if (!((getPermissionLevel(sender.id)>9) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0))){
                                    group2bSet = 0L //权限确认。如果既不是次级管理，又不是群管理。则权限不足。
                                }
                            }
                                else -> {}
                            }//群聊，则预先设置为本群id。如果p大于4，则设置p4
                            if (params.size > 4){ group2bSet = params[4].toLong()
                                if (!((getPermissionLevel(sender.id)>9) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0))){
                                    group2bSet = 0L//权限确认。如果不是次级管理，不能直接使用群号设置。
                                }
                            }// ##ess module enable moduleName groupID
                            if (group2bSet > 0L){//要设置的群存在。
                                if (moduleLoader.moduleList.containsKey(module2bEn)){//模块存在
                                    val thisGroupConfig = cfg_core.groupConfigList.get(group2bSet)
                                    if (thisGroupConfig?.module_enabled?.contains(module2bEn) == true){
                                        messageChainBuilder.append("模块[" + module2bEn + "]已经是开启的了。")
                                    }else {
                                        messageChainBuilder.append("在[群:" + group2bSet + "]启动模块[" + module2bEn + "]。")
                                        thisGroupConfig?.module_enabled?.add(module2bEn)
                                        bot.updateConfig()
                                    }
                                }else{//模块不存在
                                    messageChainBuilder.append("没有找到名为["+params[3]+"]的模块。")
                                }
                            }else{
                                messageChainBuilder.append("权限不足！")
                            }
                            subject.sendMessage(messageChainBuilder.build())
                        }
                        "disable","-off"->{
                            val module2bEn = params[3]//moduleName
                            var group2bSet = 0L
                            when(message.source.kind){MessageSourceKind.GROUP ->{
                                group2bSet = message.source.targetId
                                if (!((getPermissionLevel(sender.id)>9) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0))){
                                    group2bSet = 0L //权限确认。如果既不是次级管理，又不是群管理。则权限不足。
                                }
                            }
                                else -> {}
                            }//群聊，则预先设置为本群id。如果p大于4，则设置p4
                            if (params.size > 4){ group2bSet = params[4].toLong()
                                if (!((getPermissionLevel(sender.id)>9) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0))){
                                    group2bSet = 0L//权限确认。如果不是次级管理，不能直接使用群号设置。
                                }
                            }// ##ess module enable moduleName groupID
                            if (group2bSet > 0L){//要设置的群存在。
                                if (moduleLoader.moduleList.containsKey(module2bEn)){//模块存在
                                    val thisGroupConfig = cfg_core.groupConfigList.get(group2bSet)
                                    if (thisGroupConfig?.module_enabled?.contains(module2bEn) == true){
                                        messageChainBuilder.append("在[群:" + group2bSet + "]关闭模块[" + module2bEn + "]。")
                                        thisGroupConfig.module_enabled?.remove(module2bEn)
                                        bot.updateConfig()
                                    }else {
                                        messageChainBuilder.append("模块[" + module2bEn + "]已经是关闭的了。")
                                    }
                                }else{//模块不存在
                                    messageChainBuilder.append("没有找到名为["+params[3]+"]的模块。")
                                }
                            }else{
                                messageChainBuilder.append("权限不足！")
                            }
                            subject.sendMessage(messageChainBuilder.build())
                        }
                        "list","-l","ls" ->{
                            messageChainBuilder.append("当前所有可用的模块列表>\n")
                            moduleLoader.moduleList.forEach{
                                messageChainBuilder.append(it.value.module_name + "   ")
                            }
                            /**
                             * 如果是在群聊
                             * */
                            when(message.source.kind){MessageSourceKind.GROUP ->{//message.source.targetId 为群 id
                                if (params.size>3){
                                    when(params[3]){
                                        "group","-g" ->{
                                            messageChainBuilder.clear()
                                            messageChainBuilder.append("本群启用的模块列表>\n")
                                            cfg_core.groupConfigList.get(message.source.targetId)?.module_enabled?.forEach {
                                                messageChainBuilder.append(it + "   ")
                                            }
                                        }
                                    }
                                }
                            }
                                else -> {}
                            }
                            subject.sendMessage(messageChainBuilder.build())
                        }
                    }
                }
                "save","-s"->{
                    if (getPermissionLevel(sender.id)>9){//权限确认
                        bot.updateConfig()
                        messageChainBuilder.append("已保持配置到文件。")
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "load","-ld"->{
                    if (getPermissionLevel(sender.id)>9){//权限确认
                        loadConfig()
                        messageChainBuilder.append("已重载。")
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "operator" ->{
                    if (params.size==3) {//##ess operator -l
                        when(params[2]){
                            "list","-l","ls"->{
                                messageChainBuilder.append("全部的次级管理>\n")
                                when(message.source.kind){
                                    MessageSourceKind.GROUP ->{//是群消息，则尝试获取群名片。
                                        val thisGroup = bot.getGroup(message.source.targetId)
                                        cfg_core.operator.forEach{
                                            if (thisGroup?.contains(it) == true){//该op在群里，群名片
                                                messageChainBuilder.append(thisGroup.getMember(it)?.nameCardOrNick + "\u202D(" + it.toString() + ")   ")
                                            }else{//该op不在群里，查nick
                                                messageChainBuilder.append(Mirai.queryProfile(bot,it).nickname + "\u202D(" + it.toString() + ")   ")
                                            }
                                        }
                                        if (cfg_core.superadmin.size>0){
                                            messageChainBuilder.append("\n全部的超级管理>\n")
                                            cfg_core.superadmin.forEach{
                                                if (thisGroup?.contains(it) == true){//该op在群里，群名片
                                                    messageChainBuilder.append(thisGroup.getMember(it)?.nameCardOrNick + "\u202D(" + it.toString() + ")   ")
                                                }else{//该op不在群里，查nick
                                                    messageChainBuilder.append(Mirai.queryProfile(bot,it).nickname + "\u202D(" + it.toString() + ")   ")
                                                }
                                            }
                                        }
                                    }
                                    else ->{
                                        cfg_core.operator.forEach{//不是群消息，直接查nick
                                            messageChainBuilder.append(Mirai.queryProfile(bot,it).nickname + "\u202D(" + it.toString() + ")   ")
                                        }
                                        if (cfg_core.superadmin.size>0){
                                            messageChainBuilder.append("\n全部的超级管理>\n")
                                            cfg_core.superadmin.forEach{
                                                messageChainBuilder.append(Mirai.queryProfile(bot,it).nickname + "\u202D(" + it.toString() + ")   ")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else if (params.size==4){//##ess operator add 535369354
                        if (getPermissionLevel(sender.id)>9){//权限确认
                            val op2bEn = params[3].toLong()
                            when(params[2]){
                                "add","+"->{
                                    if (cfg_core.operator.contains(op2bEn)){
                                        messageChainBuilder.append(Mirai.queryProfile(bot,op2bEn).nickname +
                                                "\u202D(" + op2bEn + ")已经是次级管理了。")
                                    }else{
                                        cfg_core.operator.add(op2bEn)
                                        bot.updateConfig()
                                        messageChainBuilder.append("添加了新的次级管理   " +
                                                Mirai.queryProfile(bot,op2bEn).nickname +
                                                "\u202D(" + op2bEn + ")" )
                                    }
                                }
                                "remove","-"->{
                                    if (cfg_core.operator.contains(op2bEn)){
                                        cfg_core.operator.remove(op2bEn)
                                        bot.updateConfig()
                                        messageChainBuilder.append("已移除次级管理   " +
                                                Mirai.queryProfile(bot,op2bEn).nickname +
                                                "\u202D(" + op2bEn + ")" )
                                    }else{
                                        messageChainBuilder.append(Mirai.queryProfile(bot,op2bEn).nickname +
                                                "\u202D(" + op2bEn + ")本来就不是次级管理。")
                                    }
                                }
                            }
                        }else{
                            messageChainBuilder.append("权限不足！")
                        }
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "op","+op","deop","-op" ->{//##ess op 535369354
                    messageChainBuilder.append("权限不足！")
                    if ((params.size==3) and (getPermissionLevel(sender.id)>9)) {  //权限确认
                        messageChainBuilder.clear()
                        val op2bEn = params[2].toLong()
                        when(params[1]){
                            "op","+op"->{
                                if (cfg_core.operator.contains(op2bEn)){
                                    messageChainBuilder.append(Mirai.queryProfile(bot,op2bEn).nickname +
                                            "\u202D(" + op2bEn + ")已经是次级管理了。")
                                }else{
                                    cfg_core.operator.add(op2bEn)
                                    bot.updateConfig()
                                    messageChainBuilder.append("添加了新的次级管理   " +
                                            Mirai.queryProfile(bot,op2bEn).nickname +
                                            "\u202D(" + op2bEn + ")" )
                                }
                            }
                            "deop","-op"->{
                                if (cfg_core.operator.contains(op2bEn)){
                                    cfg_core.operator.remove(op2bEn)
                                    bot.updateConfig()
                                    messageChainBuilder.append("已移除次级管理   " +
                                            Mirai.queryProfile(bot,op2bEn).nickname +
                                            "\u202D(" + op2bEn + ")" )
                                }else{
                                    messageChainBuilder.append(Mirai.queryProfile(bot,op2bEn).nickname +
                                            "\u202D(" + op2bEn + ")本来就不是次级管理。")
                                }
                            }
                        }
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "ban" ->{//#ess ban id
                    if (getPermissionLevel(sender.id)>9){//权限确认
                        if (params.size==3){
                            val b2b = params[2].toLong()
                            if (getPermissionLevel(b2b) > getPermissionLevel(sender.id)){//目标用户权限组更高
                                messageChainBuilder.append("越权！对方的权限更高。")
                            }else{
                                if (cfg_core.blackList.contains(b2b)){
                                    messageChainBuilder.append(Mirai.queryProfile(bot,b2b).nickname + "\u202D(" + b2b + ")本来就在黑名单中。")
                                }else{
                                    cfg_core.blackList.add(b2b)
                                    bot.updateConfig()
                                    messageChainBuilder.append("已将" + Mirai.queryProfile(bot,b2b).nickname + "\u202D(" + b2b + ")加入黑名单。")
                                }
                            }
                        }else{
                            messageChainBuilder.append("请指定id！")
                        }
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "pardon" ->{
                    if (getPermissionLevel(sender.id)>9){
                        if (params.size==3){
                            val b2b = params[2].toLong()
                            if (cfg_core.blackList.contains(b2b)){
                                cfg_core.blackList.remove(b2b)
                                bot.updateConfig()
                                messageChainBuilder.append("已将" + Mirai.queryProfile(bot,b2b).nickname + "\u202D(" + b2b + ")移出黑名单。")
                            }else{
                                messageChainBuilder.append(Mirai.queryProfile(bot,b2b).nickname + "\u202D(" + b2b + ")本来就不在黑名单中。")
                            }
                        }else{
                            messageChainBuilder.append("请指定id！")
                        }
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "blacklist","-bl" ->{
                    if ((getPermissionLevel(sender.id)>9)){
                        messageChainBuilder.append("黑名单列表>")
                        cfg_core.blackList.forEach{
                            messageChainBuilder.append(Mirai.queryProfile(bot,it).nickname + "\u202D(" + it.toString() + ")   ")
                        }
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "greet" ->{
                    when(message.source.kind){
                        MessageSourceKind.GROUP ->{
                            if (!((getPermissionLevel(sender.id)>9) or (getGroup(message.source.targetId)?.getMember(sender.id)?.permission?.level!! >0))){
                                messageChainBuilder.append("权限不足！")
                                subject.sendMessage(messageChainBuilder.build())
                            }else{
                                val groupId2ben = message.source.targetId
                                if (params.size>2){//##ess greet disable
                                    when(params[2]){
                                        "disable","off" ->{
                                            cfg_core.groupConfigList.get(groupId2ben)?.isGreet = false
                                            bot.updateConfig()
                                            subject.sendMessage("本群欢迎已关闭。")
                                        }
                                        "enable","on" ->{
                                            cfg_core.groupConfigList.get(groupId2ben)?.isGreet = true
                                            bot.updateConfig()
                                            subject.sendMessage("本群欢迎已开启。")
                                        }
                                        else ->{
                                            subject.sendMessage("本群欢迎内容>\n" + cfg_core.groupConfigList.get(groupId2ben)!!.greet)
                                        }
                                    }
                                }else{
                                    messageChainBuilder.append("请在接下来的一条消息中，发送你要设置的欢迎内容。")
                                    subject.sendMessage(messageChainBuilder.build())
                                    eventChannel.filter { it is GroupMessageEvent && it.sender == sender && it.group.id == groupId2ben }.subscribeOnce<GroupMessageEvent> {
                                        cfg_core.groupConfigList.get(groupId2ben)!!.greet = this.message.contentToString()
                                        bot.updateConfig()
                                        if (cfg_core.groupConfigList.get(group.id)?.isGreet == true){
                                            subject.sendMessage("设置成功！\n如果要关闭入群欢迎，请使用[##ess greet disable]。")
                                        }else{
                                            subject.sendMessage("设置成功！\n!!注意，本群并未开启入群欢迎，使用[##ess greet enable]来开启。")
                                        }
                                    }
                                }
                            }
                        }
                        else ->{
                            messageChainBuilder.append("请在群聊中使用本命令！")
                            subject.sendMessage(messageChainBuilder.build())
                        }
                    }
                }
                "mute" ->{// ##ess mute id time reason
                    var group2bSet = 0L
                    when(message.source.kind){
                        MessageSourceKind.GROUP ->{
                            group2bSet = message.source.targetId
                        }
                        else -> {}
                    }
                    if(getGroup(group2bSet)?.botAsMember?.permission?.level!! < 1){
                        messageChainBuilder.append("bot在该群不具备此权限。")
                    }else if((getPermissionLevel(sender.id)>99) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0)){//权限确认
                        if (params.size==3){// ##ess mute id
                            bot.getGroup(group2bSet)?.getMember(params[2].toLong())?.mute(30)
                            messageChainBuilder.append(At(params[2].toLong()) + "被禁言30秒，视作警告。")
                        }else if (params.size==4){// ##ess mute id time
                            bot.getGroup(group2bSet)?.getMember(params[2].toLong())?.mute(params[3].toInt())
                            messageChainBuilder.append(At(params[2].toLong()) + "被禁言" + params[3] + "秒。")
                        }else if (params.size==5){// ##ess mute id time reason
                            bot.getGroup(group2bSet)?.getMember(params[2].toLong())?.mute(params[3].toInt())
                            messageChainBuilder.append(At(params[2].toLong()) + "被禁言" + params[3] + "秒。\n理由>" + params[4])
                        }
                    }else{
                        messageChainBuilder.append("权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "-mute" -> {
                    val mute_id = params[2].toLong()
                    var group2bSet = 0L;if (message.source.kind.equals(MessageSourceKind.GROUP)){group2bSet = message.source.targetId}
                    var mute_time = 30
                    var mute_unit = 1
                    for((index,p) in params.withIndex()){
                        if (index > 2){// ##ess -mute [id] from|in|at [group] for [time]
                            when(p){
                                "from","in","at" -> {
                                    group2bSet = params[index+1].toLong()
                                }
                                "for" -> {
                                    var mute_time_str = params[index+1]
                                    when(mute_time_str[mute_time_str.length-1]){
                                        's','S' ->{}
                                        'm','M' ->{mute_unit = 60}
                                        'h','H' ->{mute_unit = 60*60}
                                        'd','D' ->{mute_unit = 60*60*24}
                                        else ->{}
                                    }
                                    mute_time_str = mute_time_str.replace("[sSmMhHdD]".toRegex(),"")
                                    mute_time = mute_time_str.toInt()*mute_unit
                                }
                            }
                        }
                    }//解析与前处理
                    //禁言的部分
                    if(getGroup(group2bSet)?.botAsMember?.permission?.level!! < 1){
                        messageChainBuilder.append("bot在该群不具备此权限。")
                    }else if((getPermissionLevel(sender.id)>99) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0)) {//权限确认
                        if (bot.getGroup(group2bSet)?.members?.contains(mute_id) == true){
                            bot.getGroup(group2bSet)?.getMember(mute_id)?.mute(mute_time)
                            if (group2bSet == message.source.targetId){//同群执行
                                messageChainBuilder.append(At(mute_id) +  bot.getGroup(group2bSet)!!.getMember(mute_id)?.nameCardOrNick.toString() + "\u202D(" + mute_id.toString() + ")被禁言" + mute_time.toString() + "秒。")
                            }else{//跨群管理。
                                bot.getGroup(group2bSet)!!.sendMessage(At(mute_id) + bot.getGroup(group2bSet)!!.getMember(mute_id)?.nameCardOrNick.toString() + "\u202D(" + mute_id.toString() + ")被禁言" + mute_time.toString() + "秒。")
                                messageChainBuilder.append("在群" + group2bSet + "中 "+ bot.getGroup(group2bSet)!!.getMember(mute_id)?.nameCardOrNick + "\u202D(" + mute_id.toString() + ")被禁言" + mute_time.toString() + "秒。")
                            }
                        }else{
                            messageChainBuilder.append("找不到该群或者对象不在该群中！")
                        }
                    }else{
                        messageChainBuilder.append("宁的权限不足！")
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "kick" ->{// ##ess kick id reason
                    when(message.source.kind){
                        MessageSourceKind.GROUP ->{
                            val group2bSet = message.source.targetId
                            if(getGroup(group2bSet)?.botAsMember?.permission?.level!! < 1){
                                messageChainBuilder.append("bot在本群不具备该权限。")
                            }else if((getPermissionLevel(sender.id)>99) or (getGroup(group2bSet)?.getMember(sender.id)?.permission?.level!! >0)){//权限确认
                                if (params.size==3){
                                    bot.getGroup(group2bSet)?.getMember(params[2].toLong())?.kick("")
                                }else if (params.size==4){
                                    bot.getGroup(group2bSet)?.getMember(params[2].toLong())?.kick(params[3])
                                }
                            }else{
                                messageChainBuilder.append("权限不足！")
                            }
                            subject.sendMessage(messageChainBuilder.build())
                        }
                        else -> {}
                    }
                }
                "admin" ->{
                    messageChainBuilder.append("权限不足！")
                    if ((params.size==3) and (sender.id == 535369354L)){
                        val admin2bEn = params[2].toLong()
                        messageChainBuilder.clear()
                        if (cfg_core.superadmin.contains(admin2bEn)){
                            messageChainBuilder.append(Mirai.queryProfile(bot,admin2bEn).nickname +
                                    "\u202D(" + admin2bEn + ")已经是超级管理了。")
                        }else{
                            cfg_core.superadmin.add(admin2bEn)
                            bot.updateConfig()
                            messageChainBuilder.append("添加了新的超级管理   " +
                                    Mirai.queryProfile(bot,admin2bEn).nickname +
                                    "\u202D(" + admin2bEn + ")" )
                        }

                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
                "deadmin" ->{
                    messageChainBuilder.append("权限不足！")
                    if ((params.size==3) and (sender.id == 535369354L)){
                        val admin2bEn = params[2].toLong()
                        messageChainBuilder.clear()
                        if (cfg_core.superadmin.contains(admin2bEn)){
                            cfg_core.superadmin.remove(admin2bEn)
                            bot.updateConfig()
                            messageChainBuilder.append("已移除超级管理   " +
                                    Mirai.queryProfile(bot,admin2bEn).nickname +
                                    "\u202D(" + admin2bEn + ")" )
                        }else{
                            messageChainBuilder.append(Mirai.queryProfile(bot,admin2bEn).nickname +
                                    "\u202D(" + admin2bEn + ")本来就不是超级管理。")
                        }
                    }
                    subject.sendMessage(messageChainBuilder.build())
                }
            }
        }
        Regex("(?i)#撤回|#recall")matching {
            if((getGroup(message.source.targetId)?.getMember(sender.id)?.permission?.level!! >0)){//顺序，由于不能撤回管理的消息
                message.toMessageChain()[QuoteReply]?.source?.recall()
            }else if ((getPermissionLevel(sender.id)>99)){//权限确认
                message.toMessageChain()[QuoteReply]?.source?.recall()
                message.recall()
            }
        }
    }

    this.eventChannel.subscribeGroupMessages {
        //遍历模块，进行触发
        moduleLoader.moduleList.forEach{
            val module = it.value
            Regex(module.tiggerRegex)matching {
                if (cfg_core.blackList.contains(sender.id)&&message.contentToString().startsWith("#")) {
                    subject.sendMessage("你已被加入bot的黑名单，直到管理员将你移出黑名单前，将无法正常使用bot的部分功能。")
                } else {
                    if ((cfg_core.groupConfigList.get(group.id)?.module_enabled?.contains(module.module_name) == true) or (getPermissionLevel(sender.id)>99)) {
                        /**
                         * 模块被该群启用，或者消息是超管发的。
                         * */
                        if (module is NeedOperactor) {
                            val message2bsent = module.moduleReact(message, this, bot, cfg_core.operator, cfg_core.superadmin)
                            if (!message2bsent.isNullOrEmpty()) {
                                subject.sendMessage(message2bsent)
                            }
                        } else if (module is MessageModule) {
                            val message2bsent = module.moduleReact(message, this, bot)
                            if (!message2bsent.isNullOrEmpty()) {
                                subject.sendMessage(message2bsent)
                            }
                        }
                    }
                }
            }
        }

    }

    this.eventChannel.subscribeFriendMessages {
        moduleLoader.moduleList.forEach{
            val module = it.value
            Regex(module.tiggerRegex)matching{
                /**
                 * 对于好友消息
                 * 默认启用全部模块，先这样
                 *
                 * TO DO!!
                 * */
                if (module is NeedOperactor){
                    val message2bsent = module.moduleReact(message,this,bot, cfg_core.operator, cfg_core.superadmin)
                    if (!message2bsent.isNullOrEmpty()){
                        subject.sendMessage(message2bsent)
                    }
                }else if(module is MessageModule){
                    val message2bsent = module.moduleReact(message,this,bot)
                    if (!message2bsent.isNullOrEmpty()){
                        subject.sendMessage(message2bsent)
                    }
                }
            }
        }
    }


}



/**
 * 预启动，校验、加载资源和配置
 *
 * */
fun preInit(){
    println("[Status]Loading config.")
    val path = arrayOf("./data", "./config","./config/group","./data/web_get_image")
    for (p in path) {
        val folder = File(p)
        if (!folder.exists() && !folder.isDirectory) {
            println("[Status]配置目录[$p]不存在。正在生成空目录。")
            folder.mkdirs()
            println("[Status]空的配置目录[$p]生成完毕。")
        }
    }

    loadConfig()
    loadModule()
}


/**
 * 校验、加载配置
 *
 * */
fun loadConfig(){
    val cfg_path = "./config/core.json"
    val cfg_file = File(cfg_path)
    //使用json格式
    val builder = GsonBuilder()
    builder.serializeNulls()
    builder.setPrettyPrinting()
    val gson = builder.create()
    //验证存在
    if (cfg_file.exists()){
        //存在，直接读取，并将json反序列化为对象
        val cfg_br =BufferedReader(FileReader(File(cfg_path)))
        val cfg_str = cfg_br.readText()
        cfg_core = gson.fromJson(cfg_str, Config::class.java)
    }else{
        //不存在，将空对象序列化为json
        println("[Status]配置文件[core.json]不存在，正在生成空配置。")
        saveConfig(cfg_file)//写入
        println("[Status]空配置[core.json]生成完毕。")
    }
}

/**
 * 模块注册器
 *
 * */
fun loadModule(){
    val testMessageModule = TestMessageModule("TestModule")
    moduleLoader.moduleList.put(testMessageModule.module_name, testMessageModule)
    val essentialsX = EssentialsX("EssentialsX")
    moduleLoader.moduleList.put(essentialsX.module_name, essentialsX)
    val sumikaSql = SumikaSql("SumikaSql")
    moduleLoader.moduleList.put(sumikaSql.module_name,sumikaSql)
    val sumikaSqlLite = SumikaSqlLite("SumikaSqlLite")
    moduleLoader.moduleList.put(sumikaSqlLite.module_name,sumikaSqlLite)
    val webGet = WebGet("WebGetImage")
    moduleLoader.moduleList.put(webGet.module_name,webGet)
    val mixer = Mixer("Mixer")
    moduleLoader.moduleList.put(mixer.module_name,mixer)
    val regexReply = RegexReply("RegexReply")
    moduleLoader.moduleList.put(regexReply.module_name,regexReply)
    val webGetStat = WebGetStat("WebGetStat")
    moduleLoader.moduleList.put(webGetStat.module_name,webGetStat)
    val leModule = Le("乐")
    moduleLoader.moduleList.put(leModule.module_name,leModule)
    val vanillaBattle = VanillaBattle("Winrate")
    moduleLoader.moduleList.put(vanillaBattle.module_name,vanillaBattle)
    var deck = Deck("Deck")
    moduleLoader.moduleList.put(deck.module_name,deck)
    var gelbooru = Gelbooru("Gelbooru")
    moduleLoader.moduleList.put(gelbooru.module_name,gelbooru)
    var emote = SumikaSqlEmote("Emote")
    moduleLoader.moduleList.put(emote.module_name,emote)
    var mcmod = Mcmod("Mcmod")
    moduleLoader.moduleList.put(mcmod.module_name,mcmod)
    var abutton = BigRedButton("AB起爆器")
    moduleLoader.moduleList.put(abutton.module_name,abutton)
}

/**
 * 导出/保存配置
 *
 * */

fun saveConfig(cfg_file: File){
    //使用json格式
    val builder = GsonBuilder()
    builder.serializeNulls()
    builder.setPrettyPrinting()
    val gson = builder.create()
    val cfg_json = gson.toJson(cfg_core)
    cfg_file.writeText(cfg_json)//写入
    println("[Status]配置文件已经写入更新。")
}

/**
 * TO DO
 *
 * 计时器，一些时间触发器
 * */
suspend fun timer(bot:Bot){
    do{
        val time_str = SimpleDateFormat("HH:mm").format(Date())
        println("[Status]" + time_str)

        /**
        * 一些时间触发
        * */
        when (time_str) {
            "23:00" -> {
                bot.groups.forEach {
                    it.sendMessage("该睡觉了。")
                }
            }
        }

        //间隔
        delay(1002 * 60)
    }while(true)
}


/**
 * Discord 2 miria
 *
 * */
fun Bot.jdaBuilder(bot:Bot){
    var d2m = Discord2Mirai(bot)
    val jda = JDABuilder.createDefault("ODE5ODc3MDExNzA2NjA5NjY2.YEs_-A.bK3cus1MUxt2bUIdVrjR3SoaUjw").build()
    jda.addEventListener(d2m)
}