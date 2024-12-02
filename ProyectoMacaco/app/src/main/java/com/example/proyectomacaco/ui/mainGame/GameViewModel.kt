package com.example.proyectomacaco.ui.mainGame

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectomacaco.R

class GameViewModel : ViewModel() {

    private val _bananaCount = MutableLiveData<Int>().apply { value = 0 }
    val bananaCount: LiveData<Int> get() = _bananaCount

    private val _experience = MutableLiveData<Int>().apply { value = 0 }
    val experience: LiveData<Int> get() = _experience

    private val _rank = MutableLiveData<String>().apply { value = "Recruit" }
    val rank: LiveData<String> get() = _rank

    private val _expToNextRank = MutableLiveData<Int>().apply { value = 10 }
    val expToNextRank: LiveData<Int> get() = _expToNextRank

    private var _backgroundMediaPlayer: MediaPlayer? = null
    val backgroundMediaPlayer: MediaPlayer?
        get() = _backgroundMediaPlayer

    private val rankThresholds = listOf(
        0,
        50,
        150,
        400,
        1000,
        2500,
        6000,
        15000,
        40000,
        100000
    )

    private val rankTitles = listOf(
        "Banana Scout",
        "Tree Climber",
        "Coconut Thrower",
        "Jungle Explorer",
        "Banana Hoarder",
        "Primate Warrior",
        "Chimp Commander",
        "Space Monkey",
        "King of the Jungle",
        "Monkey Legend"
    )

    fun getRankIndex(): Int {
        val experience = _experience.value ?: 0
        return rankThresholds.indexOfLast { experience >= it }
    }

    private val _bananasSpent = MutableLiveData<Int>().apply { value = 0 }
    val bananasSpent: LiveData<Int> get() = _bananasSpent

    //////////////////////////////////////////////////////////////////////////////////////////////
    //FRAGMENTO JUEGO
    //////////////////////////////////////////////////////////////////////////////////////////////

    fun initializeMediaPlayer(context: Context, mediaResId: Int) {
        _backgroundMediaPlayer?.release()
        _backgroundMediaPlayer = MediaPlayer.create(context, mediaResId).apply {
            isLooping = true
            start()
        }
    }

    fun stopBackgroundMediaPlayer() {
        _backgroundMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        _backgroundMediaPlayer = null
    }

    fun setBananaCount(count: Int) {
        _bananaCount.value = count
    }

    fun setExperience(exp: Int) {
        _experience.value = exp
        updateRank(exp)
    }

    fun increaseBananaCount() {
        _bananaCount.value = (_bananaCount.value ?: 0) + 1
        updateExperience()
    }

    private fun updateExperience() {
        val bananasEarned = (_bananaCount.value ?: 0) + (_bananasSpent.value ?: 0)
        val newExperience = bananasEarned / 10
        if (newExperience > (_experience.value ?: 0)) {
            _experience.value = newExperience
            updateRank(newExperience)
        }
    }

    private fun updateRank(exp: Int) {
        val rankIndex = rankThresholds.indexOfLast { it <= exp }
        _rank.value = rankTitles.getOrElse(rankIndex) { "General" }

        val nextThreshold = rankThresholds.getOrElse(rankIndex + 1) { exp }
        _expToNextRank.value = nextThreshold - exp
    }

    override fun onCleared() {
        super.onCleared()
        _backgroundMediaPlayer?.release()
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //FRAGMENTO MEJORAS
    //////////////////////////////////////////////////////////////////////////////////////////////
    private val improvementLevels = MutableLiveData<MutableMap<String, Int>>().apply {
        value = mutableMapOf(
            "efficiency" to 0,
            "passive" to 0,
            "afk" to 0
        )
    }
    val improvements: MutableLiveData<MutableMap<String, Int>> get() = improvementLevels

    private val improvementCosts = MutableLiveData<MutableMap<String, Int>>().apply {
        value = mutableMapOf(
            "efficiency" to 50,
            "passive" to 100,
            "afk" to 200
        )
    }
    val costs: MutableLiveData<MutableMap<String, Int>> get() = improvementCosts

    private val maxLevels = mapOf(
        "efficiency" to 8,
        "passive" to 8,
        "afk" to 8
    )

    fun isImprovementMaxed(type: String): Boolean {
        return (improvementLevels.value?.get(type) ?: 0) >= (maxLevels[type] ?: Int.MAX_VALUE)
    }

    fun buyImprovement(type: String): Boolean {
        if (isImprovementMaxed(type)) return false

        val currentBananas = _bananaCount.value ?: 0
        val costs = improvementCosts.value ?: return false
        val levels = improvementLevels.value ?: return false

        val cost = costs[type] ?: return false
        return if (currentBananas >= cost) {
            _bananaCount.value = currentBananas - cost
            _bananasSpent.value = (_bananasSpent.value ?: 0) + cost
            levels[type] = (levels[type] ?: 0) + 1
            costs[type] = (cost * 2.5).toInt()
            improvementLevels.value = levels
            improvementCosts.value = costs
            true
        } else {
            false
        }
    }

    fun saveImprovements(sharedPreferences: SharedPreferences) {
        val levels = improvementLevels.value ?: return
        val costs = improvementCosts.value ?: return
        val editor = sharedPreferences.edit()

        levels.forEach { (type, level) ->
            editor.putInt("improvement_${type}_level", level)
        }

        costs.forEach { (type, cost) ->
            editor.putInt("improvement_${type}_cost", cost)
        }

        editor.putInt("bananas_spent", _bananasSpent.value ?: 0)
        editor.apply()
    }


    fun loadImprovements(sharedPreferences: SharedPreferences) {
        val levels = improvementLevels.value ?: return
        val costs = improvementCosts.value ?: return

        levels.keys.forEach { type ->
            levels[type] = sharedPreferences.getInt("improvement_${type}_level", 0)
            costs[type] = sharedPreferences.getInt("improvement_${type}_cost", initialCost(type))
        }

        _bananasSpent.value = sharedPreferences.getInt("bananas_spent", 0)
        improvementLevels.value = levels
        improvementCosts.value = costs
    }


    private fun initialCost(type: String): Int {
        return when (type) {
            "efficiency" -> 50
            "passive" -> 100
            "afk" -> 200
            else -> 0
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    fun buyMediaPlayer(context: Context, soundResource: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResource)
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
        mediaPlayer?.start()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //FRAGMENTO COSMETICOS
    //////////////////////////////////////////////////////////////////////////////////////////////

    data class Monkey(
        val id: String,
        val name: String,
        val imageRes: Int,
        val cost: Int,
        var isPurchased: Boolean = false,
        var isEquipped: Boolean = false
    )

    private val _cosmetics = MutableLiveData<List<Monkey>>().apply {
        value = listOf(
            Monkey("chimp", "Chimpanzee", R.drawable.monkey_left, 0, isPurchased = true, isEquipped = true),
            Monkey("gorilla", "Gorilla", R.drawable.gorilla_left, 200),
            Monkey("orangutan", "Orangutan", R.drawable.orangutan_left, 600),
            Monkey("macaque", "Macaque", R.drawable.macaque_left, 800),
            Monkey("albino", "Albino Gorilla", R.drawable.albino_left, 3000)
        )
    }
    val cosmetics: LiveData<List<Monkey>> get() = _cosmetics

    fun loadCosmetics(sharedPreferences: SharedPreferences) {
        val cosmeticsList = cosmetics.value?.toMutableList() ?: return

        cosmeticsList.forEach { cosmetic ->
            val isPurchased = sharedPreferences.getBoolean("cosmetic_${cosmetic.id}_purchased", false)
            val isEquipped = sharedPreferences.getBoolean("cosmetic_${cosmetic.id}_equipped", false)

            if (cosmetic.id == "chimp" && !sharedPreferences.contains("cosmetic_chimp_purchased")) {
                cosmetic.isPurchased = true
                cosmetic.isEquipped = true
            } else {
                cosmetic.isPurchased = isPurchased
                cosmetic.isEquipped = isEquipped
            }
        }

        _bananasSpent.value = sharedPreferences.getInt("bananas_spent", 0)
        _cosmetics.value = cosmeticsList
    }


    fun saveCosmetics(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        cosmetics.value?.forEach { cosmetic ->
            editor.putBoolean("cosmetic_${cosmetic.id}_purchased", cosmetic.isPurchased)
            editor.putBoolean("cosmetic_${cosmetic.id}_equipped", cosmetic.isEquipped)
        }
        editor.putInt("bananas_spent", _bananasSpent.value ?: 0)
        editor.apply()
    }

    fun buyCosmetic(type: String): Boolean {
        val cosmetic = cosmetics.value?.find { it.id == type } ?: return false

        if (!cosmetic.isPurchased) {
            val currentBananas = _bananaCount.value ?: 0
            if (currentBananas >= cosmetic.cost) {
                _bananaCount.value = currentBananas - cosmetic.cost
                _bananasSpent.value = (_bananasSpent.value ?: 0) + cosmetic.cost
                cosmetic.isPurchased = true

                _cosmetics.value = _cosmetics.value?.map { if (it.id == type) cosmetic else it }
                return true
            }
        }
        return false
    }

    fun equipCosmetic(id: String) {
        val monkeyList = _cosmetics.value?.toMutableList() ?: return

        monkeyList.forEach { it.isEquipped = false }

        monkeyList.find { it.id == id }?.isEquipped = true

        _cosmetics.value = monkeyList
    }

    fun resetCosmetics() {
        val defaultCosmetics = listOf(
            Monkey("chimp", "Chimpanzee", R.drawable.monkey_left, 0, isPurchased = true, isEquipped = true),
            Monkey("gorilla", "Gorilla", R.drawable.gorilla_left, 200),
            Monkey("orangutan", "Orangutan", R.drawable.orangutan_left, 600),
            Monkey("macaque", "Macaque", R.drawable.macaque_left, 800),
            Monkey("albino", "Albino Gorilla", R.drawable.albino_left, 3000)
        )
        _cosmetics.value = defaultCosmetics
    }
}