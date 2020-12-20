package pt.atp.bobi.presentation.ui.fragments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.atp.bobi.data.DogsAPIClient
import pt.atp.bobi.data.callback.DataRetriever
import pt.atp.bobi.data.model.Breed
import pt.atp.bobi.data.persistence.Dog
import pt.atp.bobi.data.persistence.DogRepository

class BreedsViewModel(
    private val repository: DogRepository
) : ViewModel(), DataRetriever {

    private val _dogsViewModel = MutableLiveData<List<Breed>>()
    val dogsLiveData = _dogsViewModel

    private var dogsLoaded = emptyList<Breed>()

    fun loadDogs() {
        DogsAPIClient.getListOfBreeds(this)
    }

    fun favBreed(breed: Breed) {
        val dog = breedToDog(breed)
        repository.insert(dog)
        updateDogs()
    }

    override fun onDataFetchedSuccess(breeds: List<Breed>) {
        dogsLoaded = breeds
        updateDogs()
    }

    override fun onDataFetchedFailed() {
        Log.e(TAG, "Unable to retrieve the data")
        _dogsViewModel.postValue(emptyList())
    }


    private fun breedToDog(breed: Breed): Dog {
        return Dog(
            bredFor = breed.bredFor,
            bredGroup = breed.bredGroup,
            id = breed.id,
            lifeSpan = breed.lifeSpan,
            name = breed.name,
            origin = breed.origin,
            temperament = breed.temperament
        )
    }

    private fun updateDogs() {

        repository.getDogs { dogs ->

            val dogsIDs = dogs.map { it.id }

            dogsLoaded.map {
                if (dogsIDs.contains(it.id))
                    it.copy(fav = true)
                else
                    it
            }.let {
                _dogsViewModel.postValue(it)
            }
        }
    }
}

class BreedsViewModelFactory(
    private val repository: DogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreedsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BreedsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}