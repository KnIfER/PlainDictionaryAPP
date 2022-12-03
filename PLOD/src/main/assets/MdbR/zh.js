(function(){
	//简繁对照表
	var zh_s = '㑩㓥㔉㖊㖞㟆㧑㧟㨫㱩㱮㲿㳠㶉㶶㶽㺍䁖䅉䇲䌶䌷䌸䌹䌺䌼䌽䌾䍀䍁䓕䗖䙓䜣䜧䝙䞍䞐䥺䥽䦃䦅䩄䯄䯅䲝䲟䲠䲡䲢䲣䴓䴔䴕䴖䴗䴘䴙万与丑专业丛东丝丢两严丧个个丰临为为丽举么么么义乌乐乔习乡书买乱了争于亏云亘亚产产亩亲亵亸亿仅仆仇从仑仑仓仪们价众众优伙会伛伞伟传伣伤伥伦伧伪伪伫体余佣佥侠侣侥侦侧侨侩侪侬俣俦俨俩俪俫俭债倾偬偻偾偿傥傧储傩僵儿克兑兖党兰关兴兹养兽冁内冈册写军农冢冬冯冲冲决况冻净净凄准凉凌减凑凛几凤凫凫凭凯凶出击凿刍划刘则刚创删别别刬刭刮制刹刽刿剀剂剐剑剥剧劝办务劢动励劲劳势勋勋勚匀匦匮区医千升升华协单卖卜占卢卤卤卧卫即却卷卺厂厅历历历厉压厌厍厐厕厕厘厢厣厦厨厩厮县叁参参双发发变叙叠只只台台台叶号叹叹叽吁吃合吊同后向向吓吕吗吣吨听启启吴呆呐呒呓呕呖呗员呙呛呜周咏咙咛咝咤咨咸咽哄响哑哒哓哔哕哗哙哜哝哟唇唛唝唠唡唢唤啧啬啭啮啴啸喂喷喽喾嗫嗳嘘嘤嘱噜嚣回团团园困囱围囵国图圆圣圹场坏块坚坛坛坛坛坛坜坝坞坟坠垄垅垆垒垦垩垫垭垱垲垴埘埙埚埯埼堑堕墙墙壮声壳壳壶壸处备复复够头夸夹夺奁奂奋奖奖奥奸妆妇妈妩妪妫妫姗姜姹娄娅娆娇娈娱娲娴婳婴婵婶媪嫒嫔嫱嬷孙学孪宁宝实宠审宪宫家宽宾寝对寻导寿将尔尘尝尝尧尴尸尽尽层屃屉届属屡屦屿岂岖岗岘岙岚岛岭岳岽岿峄峡峣峤峥峦崂崃崄崭嵘嵚嵝巅巩巯币布帅师帏帐帘帜带帧帮帱帻帼幂幞干干干并并幸广庄庆庐庑库应庙庞废庵廪开异弃弑张弥弥弪弯弹强归当当录录彝彦彩彻征径徕御忆忏志忧忾怀态怂怃怄怅怆怜总怼怿恋恒恳恶恶恸恹恺恻恼恽悦悫悫悬悭悮悯惊惧惨惩惫惬惭惮惯愈愠愤愦愿慑懑懒懔戆戋戏戗战戚戚戬戯户才扎扑托执扩扪扫扬扰折抚抛抟抠抡抢护报担拟拢拣拥拦拧拨择拼挂挚挛挜挝挞挟挠挡挢挣挤挥挦挨挲挽捝捞损捡换捣据掳掴掷掸掺掼揽揾揿搀搁搂搅携摄摅摆摇摈摊撄撑撵撷撸撺擞攒敌敛数斋斓斗斩断旋旗无既旧时旷旸昆昙昼昽显晋晒晓晔晕晖暂暧曲曲术术朱朴机杀杂权杆杠条来杨杩杯杰松板极构枞枢枣枥枧枨枪枫枭柜柠柽栀栅标栈栉栊栋栌栎栏树栖栗样栾桠桡桢档桤桥桦桧桨桩梁梦梼梾梿检棁棂棋棱椁椟椠椤椭楼榄榅榇榈榉榉槚槛槟槠横樯樱橥橱橹橼檩欢欤欧欲歼殁殇残殒殓殚殡殴毁毁毁毂毕毙毡毵氇气氢氩氲汇汇汇汉污汤汹沄沈沟没沣沤沥沦沧沩沩沪泄泞注泪泶泷泸泺泻泼泽泾洁洒洼浃浅浆浇浈浊测浍济浏浐浑浒浓浔涂涌涛涝涞涟涠涡涣涤润涧涨涩淀渊渌渍渎渐渑渔渖渗温游湾湿溃溅溆溯溯滗滚滞滟滠满滢滤滥滦滨滩滪漓漤潆潇潋潍潜潴澜濑濒灏灭灯灵灶灾灿炀炉炖炜炝点炼炼炽烁烂烃烛烟烦烧烨烩烫烬热焕焖焘煴爱爷牍牦牵牺犊状犷犸犹狈狝狞独狭狮狯狰狱狲猃猎猕猡猪猫猬献獭玑玚玛玮环现玱玺珐珑珰珲琏琐琼瑶瑷璎瓒瓮瓯电画画畅畴疖疗疟疠疡疬疭疮疯疱疴症痈痉痒痖痨痪痫痴瘅瘆瘗瘘瘘瘪瘫瘾瘿癞癣癫皑皱皲盏盐监盖盗盘眍眦眬睁睐睑瞆瞒瞩矫矶矾矿砀码砖砗砚砜砺砻砾础硁硕硖硗硙确硷碍碛碜碱礼祃祎祢祯祷祸禀禄禅离秃秆秋种积称秽秾稆税稣稳穑穗穷窃窍窎窑窜窝窥窦窭竖竖竞笃笋笔笕笺笼笾筑筚筛筜筝筹筼签签简箓算箦箧箨箩箪箫篑篓篮篱簖籁籴类籼粜粝粤粪粮糁糇糖系系紧累絷纟纠纡红纣纤纤纥约级纨纩纪纫纬纭纮纯纰纱纲纳纴纵纶纷纸纹纺纻纼纽纾线线绀绁绂练组绅细织终绉绊绋绌绍绎经绐绑绒结绔绕绖绗绘给绚绛络绝绝绞统绠绡绢绣绣绤绥绦绦继绨绩绪绫绬续绮绯绰绱绱绲绳维绵绶绷绷绸绹绺绻综绽绾绿绿缀缁缂缃缄缅缆缇缈缉缊缋缌缍缎缏缑缒缓缔缕编缗缘缙缚缛缜缝缞缟缠缡缢缣缤缥缦缧缨缩缪缫缬缭缮缯缰缰缱缲缳缴缵罂网罗罚罢罴羁羟翘耢耧耸耻聂聋职聍联聩聪肃肠肤肮肴肾肿胀胁胆胜胡胡胧胨胪胫胶脉脍脏脏脐脑脓脔脚脱脶脸腊腭腻腼腽腾膑臜致舆舍舣舰舱舻艰艳艳艺节芈芗芜芦芸苁苇苈苋苌苍苎苏苏苏苧苹范茎茏茑茔茕茧荆荐荙荚荛荜荞荟荠荡荡荣荤荥荦荧荨荩荪荫荬荭荮药药莅莱莲莳莴莶获获莸莹莺莼莼萝萤营萦萧萨葱蒇蒉蒋蒌蒙蒙蓑蓝蓟蓠蓣蓥蓦蔂蔑蔷蔹蔺蔼蕰蕲蕴蕴薮藓蘖虏虑虚虫虬虮虽虾虿蚀蚁蚂蚕蚬蛊蛎蛏蛮蛰蛱蛲蛳蛴蜕蜗蜡蝇蝈蝉蝎蝼蝾螀螨蟏衅衔补表衬衮袄袅袆袜袭袯装裆裈裢裣裤裥褛褴见观觃规觅视觇览觉觊觋觌觍觎觏觐觑觞触觯訚誉誊讠计订讣认讥讦讧讨让讪讫讬训议讯记讱讲讳讴讵讶讷许讹论讻讼讽设访诀证诂诃评诅识诇诈诉诊诋诌词诎诏诐译诒诓诔试诖诗诘诙诚诛诜话诞诟诠诡询诣诤该详诧诨诩诪诫诬语诮误诰诱诲诳说说诵诶请诸诹诺读诼诽课诿谀谁谂调谄谅谆谇谈谊谋谌谍谎谏谐谑谒谓谔谕谖谗谘谙谚谛谜谝谞谟谠谡谢谣谣谤谥谦谧谨谩谪谫谫谬谭谮谯谰谱谲谳谴谵谶谷豮贝贞负贠贡财责贤败账货质贩贪贫贬购贮贯贰贱贲贳贴贵贶贷贸费贺贻贼贽贾贿赀赁赂赃赃资赅赆赇赈赉赊赋赌赍赍赎赏赐赑赒赓赔赕赖赗赘赙赚赛赜赝赝赞赞赟赠赡赢赣赪赵赶趋趱趸跃跄跞践跶跷跸跹跻踊踌踪踬踯蹑蹒蹰蹿躏躜躯车轧轨轩轪轫转轭轮软轰轱轲轳轴轵轶轷轸轹轺轻轼载轾轿辀辁辂较辄辅辆辇辈辉辊辋辌辍辎辏辐辑辒输辔辕辖辗辘辙辚辞辟辩辫边辽达迁过迈运还这进远违连迟迩迳迹适选逊递逦逻遗遥邓邝邬邮邹邺邻郁郏郐郑郓郦郧郸酂酝酝酦酰酱酸酽酾酿采采采释里里鉴鉴銮錾钅钆钇针钉钊钋钌钍钎钏钐钑钒钓钔钕钖钗钘钙钚钛钜钝钞钟钟钠钡钢钣钤钥钦钧钨钩钩钪钫钬钭钮钯钰钱钲钳钴钵钵钶钷钸钹钺钻钼钽钾钿铀铁铂铃铄铅铆铇铈铉铊铋铌铍铎铏铐铑铒铓铔铕铖铗铘铙铚铛铜铝铞铟铠铡铢铣铤铥铦铧铨铩铪铫铬铭铮铯铰铱铲铲铳铴铵银铷铸铹铺铻铼铽链铿销锁锂锃锄锅锆锇锈锈锉锊锋锌锍锎锏锐锐锑锒锓锔锕锖锗锘错锚锛锜锝锞锟锠锡锢锣锤锤锥锦锧锨锨锩锪锫锬锭键锯锰锱锲锳锴锵锶锷锸锹锺锻锼锽锾锿镀镁镂镃镄镅镆镇镈镉镊镋镌镌镍镎镏镐镑镒镓镔镕镖镗镘镙镚镛镜镝镞镟镠镡镢镢镣镤镥镦镧镨镩镪镫镬镭镮镯镰镰镱镲镳镴镵镶长门闩闪闫闬闭问闯闰闱闲闲闳间闵闶闷闸闹闺闻闼闽闾闿阀阁阂阃阄阅阅阆阇阈阉阊阋阌阍阎阏阐阑阒阓阔阕阖阗阘阙阚阛队阳阴阵阶际陆陇陈陉陕陧陨险随隐隶隽难雇雏雕雠雳雾霁霉霡霭靓静面面面靥鞑鞒鞯鞲韦韧韨韩韪韫韬韵页顶顷顸项顺须须顼顽顾顿颀颁颂颃预颅领颇颈颉颊颋颌颍颎颏颐频颒颓颓颔颕颖颗题颙颚颛颜颜额颞颟颠颡颢颤颥颦颧风飏飐飑飒飓飔飕飖飗飘飙飚飞飧飨餍饣饤饥饥饦饧饨饩饪饫饬饭饮饯饰饱饲饳饴饵饶饷饸饹饺饻饼饽饾饿馀馁馂馃馄馅馆馇馈馉馊馋馌馍馎馏馐馑馒馓馔馕马驭驮驯驰驱驲驳驴驵驶驷驸驹驺驻驼驽驾驿骀骁骂骂骃骄骅骆骇骈骉骊骋验骍骎骏骐骑骒骓骔骕骖骗骘骙骚骛骜骝骞骟骠骡骢骣骤骥骦骧髅髋髌鬓魇魉鱼鱽鱾鱿鲀鲁鲂鲃鲄鲅鲆鲇鲇鲈鲉鲊鲋鲌鲍鲎鲏鲐鲑鲒鲓鲔鲕鲖鲗鲘鲙鲚鲛鲜鲝鲞鲞鲟鲠鲡鲢鲣鲤鲥鲦鲧鲨鲩鲪鲫鲬鲭鲮鲯鲰鲱鲲鲳鲴鲵鲶鲷鲸鲹鲺鲻鲼鲽鲾鲿鳀鳁鳂鳃鳄鳄鳅鳆鳇鳈鳉鳊鳋鳌鳍鳎鳏鳐鳑鳒鳓鳔鳕鳖鳗鳘鳙鳚鳛鳜鳝鳞鳟鳠鳡鳢鳣鸟鸠鸡鸡鸢鸣鸤鸥鸦鸧鸨鸩鸪鸫鸬鸭鸮鸯鸰鸱鸲鸳鸴鸵鸶鸷鸸鸹鸺鸻鸼鸽鸾鸿鹀鹁鹂鹃鹄鹅鹆鹇鹈鹉鹊鹋鹌鹍鹎鹏鹐鹑鹒鹓鹔鹕鹖鹗鹘鹙鹚鹚鹛鹜鹝鹞鹟鹠鹡鹢鹣鹤鹥鹦鹧鹨鹩鹪鹫鹬鹭鹯鹰鹱鹲鹳鹴鹾麦麸黄黉黡黩黪黾鼋鼍鼗鼹齄齐齑齿龀龁龂龃龄龅龆龇龈龉龊龋龌龙龚龛龟';
	var zh_t = '儸劏劚噚喎㠏撝擓㩜殰殨瀇澾鸂燶煱獱瞜稏筴䊷紬縳絅䋙綐綵䋻繿繸薳螮襬訢譅貙䝼賰釾鏺鐯鐥靦騧䯀䱽鮣鰆鰌鰧䱷鳾鵁鴷鶄鶪鷈鷿萬與醜專業叢東絲丟兩嚴喪個箇豐臨為爲麗舉幺麼麽義烏樂喬習鄉書買亂瞭爭於虧雲亙亞產産畝親褻嚲億僅僕讎從侖崙倉儀們價眾衆優夥會傴傘偉傳俔傷倀倫傖偽僞佇體餘傭僉俠侶僥偵側僑儈儕儂俁儔儼倆儷倈儉債傾傯僂僨償儻儐儲儺殭兒剋兌兗黨蘭關興茲養獸囅內岡冊寫軍農塚鼕馮沖衝決況凍凈淨淒準涼淩減湊凜幾鳳鳧鳬憑凱兇齣擊鑿芻劃劉則剛創刪別彆剗剄颳製剎劊劌剴劑剮劍剝劇勸辦務勱動勵勁勞勢勛勳勩勻匭匱區醫韆昇陞華協單賣蔔佔盧鹵滷臥衛卽卻捲巹廠廳歷厤曆厲壓厭厙龎廁厠釐廂厴廈廚廄廝縣叄參蔘雙發髮變敘疊衹隻檯臺颱葉號嘆歎嘰籲喫閤弔衕後嚮曏嚇呂嗎唚噸聽啟啓吳獃吶嘸囈嘔嚦唄員咼嗆嗚週詠嚨嚀噝吒諮鹹嚥鬨響啞噠嘵嗶噦嘩噲嚌噥喲脣嘜嗊嘮啢嗩喚嘖嗇囀嚙嘽嘯餵噴嘍嚳囁噯噓嚶囑嚕囂迴團糰園睏囪圍圇國圖圓聖壙場壞塊堅壇墰壜罈罎壢壩塢墳墜壟壠壚壘墾堊墊埡壋塏堖塒塤堝垵碕塹墮牆墻壯聲殼殻壺壼處備復複夠頭誇夾奪奩奐奮獎奬奧姦妝婦媽嫵嫗媯嬀姍薑奼婁婭嬈嬌孌娛媧嫻嫿嬰嬋嬸媼嬡嬪嬙嬤孫學孿寧寶實寵審憲宮傢寬賓寢對尋導壽將爾塵嘗嚐堯尷屍盡儘層屓屜屆屬屢屨嶼豈嶇崗峴嶴嵐島嶺嶽崬巋嶧峽嶢嶠崢巒嶗崍嶮嶄嶸嶔嶁巔鞏巰幣佈帥師幃帳簾幟帶幀幫幬幘幗冪襆乾幹榦並併倖廣莊慶廬廡庫應廟龐廢菴廩開異棄弒張彌瀰弳彎彈強歸當噹錄録彞彥綵徹徵徑徠禦憶懺誌憂愾懷態慫憮慪悵愴憐總懟懌戀恆懇惡噁慟懨愷惻惱惲悅愨慤懸慳悞憫驚懼慘懲憊愜慚憚慣癒慍憤憒願懾懣懶懍戇戔戲戧戰慼鏚戩戱戶纔紮撲託執擴捫掃揚擾摺撫拋摶摳掄搶護報擔擬攏揀擁攔擰撥擇拚掛摯攣掗撾撻挾撓擋撟掙擠揮撏捱挱輓挩撈損撿換搗據擄摑擲撣摻摜攬搵撳攙擱摟攪攜攝攄擺搖擯攤攖撐攆擷擼攛擻攢敵斂數齋斕鬥斬斷镟旂無旣舊時曠暘崑曇晝曨顯晉曬曉曄暈暉暫曖麯麴術朮硃樸機殺雜權桿槓條來楊榪盃傑鬆闆極構樅樞棗櫪梘棖槍楓梟櫃檸檉梔柵標棧櫛櫳棟櫨櫟欄樹棲慄樣欒椏橈楨檔榿橋樺檜槳樁樑夢檮棶槤檢梲欞棊稜槨櫝槧欏橢樓欖榲櫬櫚櫸欅檟檻檳櫧橫檣櫻櫫櫥櫓櫞檁歡歟歐慾殲歿殤殘殞殮殫殯毆毀燬譭轂畢斃氈毿氌氣氫氬氳匯彙滙漢汙湯洶澐瀋溝沒灃漚瀝淪滄溈潙滬洩濘註淚澩瀧瀘濼瀉潑澤涇潔灑窪浹淺漿澆湞濁測澮濟瀏滻渾滸濃潯塗湧濤澇淶漣潿渦渙滌潤澗漲澀澱淵淥漬瀆漸澠漁瀋滲溫遊灣濕潰濺漵泝遡潷滾滯灧灄滿瀅濾濫灤濱灘澦灕灠瀠瀟瀲濰潛瀦瀾瀨瀕灝滅燈靈竈災燦煬爐燉煒熗點煉鍊熾爍爛烴燭煙煩燒燁燴燙燼熱煥燜燾熅愛爺牘氂牽犧犢狀獷獁猶狽獮獰獨狹獅獪猙獄猻獫獵獼玀豬貓蝟獻獺璣瑒瑪瑋環現瑲璽琺瓏璫琿璉瑣瓊瑤璦瓔瓚甕甌電畫畵暢疇癤療瘧癘瘍癧瘲瘡瘋皰痾癥癰痙癢瘂癆瘓癇癡癉瘮瘞瘺瘻癟癱癮癭癩癬癲皚皺皸盞鹽監蓋盜盤瞘眥矓睜睞瞼瞶瞞矚矯磯礬礦碭碼磚硨硯碸礪礱礫礎硜碩硤磽磑確礆礙磧磣鹼禮禡禕禰禎禱禍稟祿禪離禿稈鞦種積稱穢穠穭稅穌穩穡繐窮竊竅窵窯竄窩窺竇窶豎竪競篤筍筆筧箋籠籩築篳篩簹箏籌篔簽籤簡籙祘簀篋籜籮簞簫簣簍籃籬籪籟糴類秈糶糲粵糞糧糝餱醣係繫緊纍縶糹糾紆紅紂纖縴紇約級紈纊紀紉緯紜紘純紕紗綱納紝縱綸紛紙紋紡紵紖紐紓線綫紺紲紱練組紳細織終縐絆紼絀紹繹經紿綁絨結絝繞絰絎繪給絢絳絡絕絶絞統綆綃絹綉繡綌綏絛縧繼綈績緒綾緓續綺緋綽緔鞝緄繩維綿綬綳繃綢綯綹綣綜綻綰綠緑綴緇緙緗緘緬纜緹緲緝縕繢緦綞緞緶緱縋緩締縷編緡緣縉縛縟縝縫縗縞纏縭縊縑繽縹縵縲纓縮繆繅纈繚繕繒韁繮繾繰繯繳纘罌網羅罰罷羆羈羥翹耮耬聳恥聶聾職聹聯聵聰肅腸膚骯餚腎腫脹脅膽勝衚鬍朧腖臚脛膠脈膾臟髒臍腦膿臠腳脫腡臉臘齶膩靦膃騰臏臢緻輿捨艤艦艙艫艱艷豔藝節羋薌蕪蘆蕓蓯葦藶莧萇蒼苧蘇囌甦薴蘋範莖蘢蔦塋煢繭荊薦薘莢蕘蓽蕎薈薺盪蕩榮葷滎犖熒蕁藎蓀蔭蕒葒葤葯藥蒞萊蓮蒔萵薟獲穫蕕瑩鶯蒓蓴蘿螢營縈蕭薩蔥蕆蕢蔣蔞懞矇簑藍薊蘺蕷鎣驀虆衊薔蘞藺藹薀蘄蘊藴藪蘚櫱虜慮虛蟲虯蟣雖蝦蠆蝕蟻螞蠶蜆蠱蠣蟶蠻蟄蛺蟯螄蠐蛻蝸蠟蠅蟈蟬蠍螻蠑螿蟎蠨釁銜補錶襯袞襖裊褘襪襲襏裝襠褌褳襝褲襇褸襤見觀覎規覓視覘覽覺覬覡覿覥覦覯覲覷觴觸觶誾譽謄訁計訂訃認譏訐訌討讓訕訖託訓議訊記訒講諱謳詎訝訥許訛論訩訟諷設訪訣證詁訶評詛識詗詐訴診詆謅詞詘詔詖譯詒誆誄試詿詩詰詼誠誅詵話誕詬詮詭詢詣諍該詳詫諢詡譸誡誣語誚誤誥誘誨誑說説誦誒請諸諏諾讀諑誹課諉諛誰諗調諂諒諄誶談誼謀諶諜謊諫諧謔謁謂諤諭諼讒諮諳諺諦謎諞諝謨讜謖謝謠謡謗謚謙謐謹謾謫譾謭謬譚譖譙讕譜譎讞譴譫讖穀豶貝貞負貟貢財責賢敗賬貨質販貪貧貶購貯貫貳賤賁貰貼貴貺貸貿費賀貽賊贄賈賄貲賃賂贓贜資賅贐賕賑賚賒賦賭齎賫贖賞賜贔賙賡賠賧賴賵贅賻賺賽賾贗贋贊讚贇贈贍贏贛赬趙趕趨趲躉躍蹌躒踐躂蹺蹕躚躋踴躊蹤躓躑躡蹣躕躥躪躦軀車軋軌軒軑軔轉軛輪軟轟軲軻轤軸軹軼軤軫轢軺輕軾載輊轎輈輇輅較輒輔輛輦輩輝輥輞輬輟輜輳輻輯轀輸轡轅轄輾轆轍轔辭闢辯辮邊遼達遷過邁運還這進遠違連遲邇逕跡適選遜遞邐邏遺遙鄧鄺鄔郵鄒鄴鄰鬱郟鄶鄭鄆酈鄖鄲酇醞醖醱醯醬痠釅釃釀埰寀採釋裏裡鑒鑑鑾鏨釒釓釔針釘釗釙釕釷釺釧釤鈒釩釣鍆釹鍚釵鈃鈣鈈鈦鉅鈍鈔鍾鐘鈉鋇鋼鈑鈐鑰欽鈞鎢鈎鉤鈧鈁鈥鈄鈕鈀鈺錢鉦鉗鈷缽鉢鈳鉕鈽鈸鉞鑽鉬鉭鉀鈿鈾鐵鉑鈴鑠鉛鉚鉋鈰鉉鉈鉍鈮鈹鐸鉶銬銠鉺鋩錏銪鋮鋏鋣鐃銍鐺銅鋁銱銦鎧鍘銖銑鋌銩銛鏵銓鎩鉿銚鉻銘錚銫鉸銥鏟剷銃鐋銨銀銣鑄鐒鋪鋙錸鋱鏈鏗銷鎖鋰鋥鋤鍋鋯鋨銹鏽銼鋝鋒鋅鋶鐦鐧銳鋭銻鋃鋟鋦錒錆鍺鍩錯錨錛錡鍀錁錕錩錫錮鑼錘鎚錐錦鑕杴鍁錈鍃錇錟錠鍵鋸錳錙鍥鍈鍇鏘鍶鍔鍤鍬鍾鍛鎪鍠鍰鎄鍍鎂鏤鎡鐨鎇鏌鎮鎛鎘鑷鎲鐫鎸鎳鎿鎦鎬鎊鎰鎵鑌鎔鏢鏜鏝鏍鏰鏞鏡鏑鏃鏇鏐鐔钁鐝鐐鏷鑥鐓鑭鐠鑹鏹鐙鑊鐳鐶鐲鐮鎌鐿鑔鑣鑞鑱鑲長門閂閃閆閈閉問闖閏闈閑閒閎間閔閌悶閘鬧閨聞闥閩閭闓閥閣閡閫鬮閱閲閬闍閾閹閶鬩閿閽閻閼闡闌闃闠闊闋闔闐闒闕闞闤隊陽陰陣階際陸隴陳陘陝隉隕險隨隱隸雋難僱雛鵰讎靂霧霽黴霢靄靚靜麪麫麵靨韃鞽韉韝韋韌韍韓韙韞韜韻頁頂頃頇項順須鬚頊頑顧頓頎頒頌頏預顱領頗頸頡頰頲頜潁熲頦頤頻頮頹頽頷頴穎顆題顒顎顓顏顔額顳顢顛顙顥顫顬顰顴風颺颭颮颯颶颸颼颻飀飄飆飈飛飱饗饜飠飣飢饑飥餳飩餼飪飫飭飯飲餞飾飽飼飿飴餌饒餉餄餎餃餏餅餑餖餓餘餒餕餜餛餡館餷饋餶餿饞饁饃餺餾饈饉饅饊饌饢馬馭馱馴馳驅馹駁驢駔駛駟駙駒騶駐駝駑駕驛駘驍罵駡駰驕驊駱駭駢驫驪騁驗騂駸駿騏騎騍騅騌驌驂騙騭騤騷騖驁騮騫騸驃騾驄驏驟驥驦驤髏髖髕鬢魘魎魚魛魢魷魨魯魴䰾魺鮁鮃鯰鮎鱸鮋鮓鮒鮊鮑鱟鮍鮐鮭鮚鮳鮪鮞鮦鰂鮜鱠鱭鮫鮮鮺鯗鮝鱘鯁鱺鰱鰹鯉鰣鰷鯀鯊鯇鮶鯽鯒鯖鯪鯕鯫鯡鯤鯧鯝鯢鯰鯛鯨鰺鯴鯔鱝鰈鰏鱨鯷鰮鰃鰓鱷鰐鰍鰒鰉鰁鱂鯿鰠鰲鰭鰨鰥鰩鰟鰜鰳鰾鱈鱉鰻鰵鱅䲁鰼鱖鱔鱗鱒鱯鱤鱧鱣鳥鳩雞鷄鳶鳴鳲鷗鴉鶬鴇鴆鴣鶇鸕鴨鴞鴦鴒鴟鴝鴛鷽鴕鷥鷙鴯鴰鵂鴴鵃鴿鸞鴻鵐鵓鸝鵑鵠鵝鵒鷳鵜鵡鵲鶓鵪鵾鵯鵬鵮鶉鶊鵷鷫鶘鶡鶚鶻鶖鶿鷀鶥鶩鷊鷂鶲鶹鶺鷁鶼鶴鷖鸚鷓鷚鷯鷦鷲鷸鷺鸇鷹鸌鸏鸛鸘鹺麥麩黃黌黶黷黲黽黿鼉鞀鼴齇齊齏齒齔齕齗齟齡齙齠齜齦齬齪齲齷龍龔龕龜棡';
	var map_s, map_t, translated, choice;
	
	function prepareMap(t){
		if(t==1 && !map_s) {
			map_s = window.Map?new Map():{};
			for(var i=0,len=zh_s.length; i<len; i++){
				map_s[zh_s[i]] = zh_t[i];
			}
		}
		if(t==2 && !map_t) {
			map_t = window.Map?new Map():{};
			for(var i=0,len=zh_s.length; i<len; i++){
				map_t[zh_t[i]] = zh_s[i];
			}
		}
	}
	
	function tradition(s) {
		var r = '',c,p;
		for(var i=0, l=s.length;i<l;i++){
			c = s.charAt(i);
			p = map_s[c];
			r += p? p : c;
		}
		return r;
	}
	
	function simplify(t) {
		var r = '',c,p;
		for(var i=0, l=t.length;i<l;i++){
			c = t.charAt(i);
			p = map_t[c];
			r += p? p : c;
		}
		return r;
	}
	
	function T(s) {
		if(s) {
			for(var i=0, l=s.length;i<l;i++){
				if(map_s[s.charAt(i)]) return 1;
			}
		}
		return 0;
	}
	function S(s) {
		if(s) {
			for(var i=0, l=s.length;i<l;i++){
				if(map_t[s.charAt(i)]) return 1;
			}
		}
		return 0;
	}
	
	function getNextNode(n, e) {
		var a = n.firstChild;
		if (a) return a;
		while (n && n!=e) {
			if (a = n.nextSibling) {
				return a
			}
			n = n.parentNode
		}
		return 0;
	}
	
	function zhTran(t, rootNode) {
		debug('zhTran', rootNode)
		var e=rootNode,n=e, fn=t==1?tradition:simplify, tn=t==1?T:S;
		if(t==0) {
			if(translated) {
				while(n=getNextNode(n,e)) {
					if (n.nodeType==3 && n._zhy) {
						n.data = n._zhy;
						n._zhy = undefined;
					}
				}
				translated = 0;
			}
		} else {
			// detect pass
			while(n=getNextNode(n,e)) {
				if (n.nodeType==1) {
					if(' SCRIPT STYLE LINK IFRAME '.indexOf(' '+n.tagName+' ') > 0)
						continue;
					if (tn(n.title)) break;
					if (tn(n.alt)) break;
					if (n.tagName == "INPUT" && tn(n.value)) 
						break;
				}
				else if (n.nodeType==3 && tn(n.data))
					break;
			}
			translated = !!n;
			// convert pass
			while(n) {
				if (n.nodeType==1) {
					if(' SCRIPT STYLE LINK IFRAME '.indexOf(' '+n.tagName+' ') > 0) {
						n=getNextNode(n,e)
						continue;
					}
					if (n.title) n.title = fn(n.title);
					if (n.alt) n.alt = fn(n.alt);
					if (n.tagName == "INPUT") 
						n.value = fn(n.value);
				}
				else if (n.nodeType==3){
					var data=n.data, text=fn(data);
					if(data!=text) {
						if(!n._zhy) n._zhy = data;
						n.data = text;
					}
				}
				n=getNextNode(n,e)
			}
		}
	}
	function zh_tran(t) {
		debug('zh_tran111', window._merge);
		choice = t;
		prepareMap(t);
		if(window._merge && !window.frameElement) {
			window.processPage = function(fx){try{if(fx)zhTran(choice, fx.contentWindow.document.body)}catch(e){debug(e)}};
			for(var i=0;i<frames.length;i++) {
				processPage(frames[i].item);
			}
		} else {
			zhTran(t, document.body);
		}
	}
	window.zh_tran = zh_tran;
})()