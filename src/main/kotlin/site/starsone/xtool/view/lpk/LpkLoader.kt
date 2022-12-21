package site.starsone.test.lpk

import com.google.gson.Gson
import org.apache.commons.codec.digest.DigestUtils
import site.starsone.xtool.view.lpk.LpkUtils
import tornadofx.*
import java.io.*
import java.util.*
import java.util.zip.ZipFile


/**
 * @author starsone
 * @date 2022/12/20 13:57
 */
fun main(args: Array<String>) {


    val lpkLoader = LpkLoader()
}

class LpkLoader {

    val configMlveFileName = DigestUtils.md5Hex("config.mlve")

    val outputPath = "D:\\temp\\live2d\\output\\"

    //对应json文件
    val jsonFilePath = "D:\\temp\\live2d\\1904090561722925675\\config.json"

    //lpk的文件
    val lpkFilePath = "D:\\temp\\live2d\\1904090561722925675\\1904090561722925675.lpk"

    private val trans: HashMap<String, String> = HashMap()
    private val entries: HashMap<String, String> = HashMap()

    var lpk: ZipFile
    var mlveConfig: MlveConfig
    var dataConfig: DataConfig

    init {
        lpk = ZipFile(lpkFilePath)
        //读取压缩包里的对应的json文件，文件名是“config.mlve”的md5
        val entry = lpk.getEntry(configMlveFileName)
        val inputStream = lpk.getInputStream(entry)

        //lpk里的配置json信息和外层的json文件信息
        //{"type":"STM_1_0","name":"Haru","id":"1658125691372","encrypt":"true","version":"1.0","list":[{"id":"","character":"character","avatar":"4a301072dec6b6a49050e5b294cd7983","costume":[{"name":"costume","path":"5bf7a047436d02a27301611a857f4644.bin"}]}]}
        var tempJson = inputStream.toJSON().toString()
        mlveConfig = Gson().fromJson(tempJson, MlveConfig::class.java)
        //{"lpkFile":"1904090561722925675.lpk","file":"","previewFile":"20220718152811.png","fileId":"1904090561722925675","type":0,"stereoMode":0,"title":"Haru","author":"oukaitou","description":"Free Material License: \nhttps://www.live2d.com/eula/live2d-free-material-license-agreement_en.html","metaData":"2de395aa-9216-4497-b5df-acb9a66f8856"}
        tempJson = File(jsonFilePath).inputStream().toJSON().toString()
        dataConfig = Gson().fromJson(tempJson, DataConfig::class.java)

        println(mlveConfig.toString())
        println(dataConfig.toString())

        //lpk中获取资源列表
        //[{"id":"","character":"character","avatar":"4a301072dec6b6a49050e5b294cd7983","costume":[{"name":"costume","path":"5bf7a047436d02a27301611a857f4644.bin"}]}]
        val characterInfoList = mlveConfig.list
        val characterInfo = characterInfoList[0]

        val subdir = outputPath + characterInfo.character
        val costume = characterInfo.costume
        costume.forEachIndexed { index, costume ->
            extractCostume(costume, subdir, index)
        }

        entries.forEach { name, u ->
            var out =u
            for (k in trans.keys) {
                out = out.replace(k, trans[k].toString())
            }
            File(subdir+name).writeText(out)
        }
    }

    fun extractCostume(costume: Costume, subdir: String, id: Int) {
        val filename = costume.path
        val json = String(decryptFile(filename))
        //{"version":"Sample 1.0.0","type":0,"model":"1dd320798d5837aa677011e8f682d806.bin","textures":["8044c735b316dc76c8ddaadc1797a7d2.bin","f86ed16a5e2a87c3d811471ca2659fbe.bin","9c6bfa7391189c5093badbb2293b9b5d.bin"],"controllers":{"param_hit":{},"param_loop":{},"key_trigger":{},"param_trigger":{},"area_trigger":{},"hand_trigger":{},"eye_blink":{"min_interval":500,"max_interval":6000,"enabled":true},"lip_sync":{"gain":5.0},"mouse_tracking":{"smooth_time":0.15,"enabled":true},"auto_breath":{"enabled":true},"extra_motion":{"enabled":true},"accelerometer":{"enabled":true},"microphone":{},"transform":{},"face_tracking":{"enabled":true},"hand_tracking":{},"param_value":{},"part_opacity":{},"artmesh_opacity":{},"artmesh_color":{},"artmesh_culling":{"default_mode":0},"intimacy_system":{}},"motions":{"idle":[{"file":"bacff4190dbb4c493a784bbb7402658a.bin"},{"file":"e66112815b78db2f64ff8d788b109a9b.bin"},{"file":"78d1a77723e4e8fdee1ed627b00835dc.bin"}],"tap":[{"file":"4894c1d821b86f8c4535d38996d84ade.bin"},{"file":"6f7f043ac0b9c94c38090eef807b7f84.bin","fade_in":500},{"file":"ee5cc129d1384c2d2052cff89256b8fd.bin"},{"file":"53ca20f66ae5996243e7cfd8b6877870.bin"},{"file":"51dae4ccaad1754b97574aa04aba8676.bin"},{"file":"00beedb6a9c9fd987f787f30da6c6b62.bin"},{"file":"99400e2f6d14058498bb9e625ef92d77.bin","fade_in":300,"fade_out":500},{"file":"f55c72ee8489cac1091ffa848509dcbd.bin"},{"file":"a066ab04303737e29088778abcb80348.bin"},{"file":"7160cd786fc8bc9581f73f3eda4a09ff.bin"},{"file":"676f1c6d3e79c51c45b47c3b707abd09.bin","sound":"9e62eb908b8e07b2ef5d8149fb68f67d.bin"},{"file":"1bda477289f2e84ab615e30fe85945b5.bin","sound":"7d301abdee01b2b9f5edb4cc8d121651.bin"},{"file":"5336b9cf86ba8fc975b8e63ef76915e4.bin","sound":"83e35274f5e55040d88a3cf21c26895c.bin","fade_in":500},{"file":"b755316b71339d23020f1028cf86d14c.bin","sound":"2870a37670b852d4a3b0d4dc43c38a51.bin"},{"file":"b122b21a08cc51ff5e7402e81103d15f.bin","sound":"3ac6a2f43e41655213f58fd72c36bcff.bin"},{"file":"e17794d537a521d9191a607a301c99db.bin","sound":"de658dea999e6c8ff926f431464aa47d.bin"},{"file":"3392e65ec5fd4f8092e9ec525bc13670.bin","sound":"5d9373f61086fff537769ff06554d7d2.bin","fade_in":700},{"file":"2df868292ec55995e76adb5d3d4d025c.bin","sound":"56002fe807a7681d30eac0ec9f72eef1.bin","fade_in":500},{"file":"1af939616bdf2fd81c2974b854dcb023.bin","sound":"aac1278683e5f5cd062afd1bf43b1dbb.bin"},{"file":"b5d0fe681297c993d56624dd5f180162.bin","sound":"3f6c1bdce86d4470ebb3bdc18c4ce996.bin"}]},"expressions":[{"name":"f01.exp.json","file":"026892da7de5d2c28fc69338a9a2c90c.bin"},{"name":"f02.exp.json","file":"9669591f1966c6b4448ae86facb0e7cb.bin"},{"name":"f03.exp.json","file":"74676bc9caf0804cb3dfb49641f19c53.bin"},{"name":"f04.exp.json","file":"1521145d48d5aa3712a1d050f85c1e52.bin"},{"name":"f05.exp.json","file":"e25cfdd5524b09932a4171bd2b3d7437.bin"},{"name":"f06.exp.json","file":"32b1388cdf834612d9a86056c9e877dc.bin"},{"name":"f07.exp.json","file":"67a213ecbb248fdf7bdfebb21c3f5ea8.bin"},{"name":"f08.exp.json","file":"a43bbce480f81c6cdc17867d0ce8c539.bin"}],"physics":"cf57d22476f25bc45c23d6f242109861.bin","physics_v2":{"file":"cf57d22476f25bc45c23d6f242109861.bin"},"pose":"dd757ed401e734fe1c030b7e926ffd1e.bin","options":{"tex_fixed":true}}
        println(json)
        val entry = Gson().fromJson(json, Map::class.java) as Map<String, *>
        LpkUtils.travelsDict(entry).forEach {
            val name = it.first
            val value = it.second
            if (value is String && LpkUtils.isEncryptedFile(value)) {
                if (!trans.containsKey(value)) {
                    val tempName = name + "_" + id
                    val suffix = recovery(value, subdir + tempName)
                    trans[value] = tempName + suffix
                }

            }
        }
        val jsonFileName = "model${id}.json"
        trans[costume.path] = jsonFileName
        entries[jsonFileName]=json

        lpk.close()
    }

    private fun recovery(filename: String, output: String): String {
        val data = decryptFile(filename)
        val suffix = LpkUtils.guessType(data)
        System.out.printf("recovering %s -> %s%s%n", filename, output, suffix)
        File(output+suffix).writeBytes(data)
        return suffix
    }

    private fun decryptFile(filename: String): ByteArray {
        val data = lpk.getInputStream(lpk.getEntry(filename)).readBytes()
        val key = getKey(filename)
        return LpkUtils.decrypt(key, data)
    }

    private fun getKey(file: String): Long {
        val type = mlveConfig.type
        val encrypt = mlveConfig.encrypt
        val id = mlveConfig.id

        return when {
            type == "STD2_0" -> LpkUtils.genKey(id + file)
            type == "STM_1_0" && encrypt == "true2" -> 0L
            type == "STM_1_0" -> LpkUtils.genKey(id + dataConfig.fileId + file + dataConfig.metaData)
            else -> 0L
        }
    }
}

data class DataConfig(
        val author: String,
        val description: String,
        val `file`: String,
        val fileId: String,
        val lpkFile: String,
        val metaData: String,
        val previewFile: String,
        val stereoMode: Int,
        val title: String,
        val type: Int
)

data class MlveConfig(
        val encrypt: String,
        val id: String,
        val list: CharacterInfo,
        val name: String,
        val type: String,
        val version: String
)

class CharacterInfo : ArrayList<CharacterInfoItem>()

data class CharacterInfoItem(
        val avatar: String,
        val character: String,
        val costume: List<Costume>,
        val id: String
)

data class Costume(
        val name: String,
        val path: String
)
