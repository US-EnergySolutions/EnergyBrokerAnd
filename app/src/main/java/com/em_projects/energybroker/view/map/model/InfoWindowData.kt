package com.em_projects.energybroker.view.map.model

class InfoWindowData {

    private var image: String? = null
    private var hotel: String? = null
    private var food: String? = null
    private var transport: String? = null

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getHotel(): String? {
        return hotel
    }

    fun setHotel(hotel: String?) {
        this.hotel = hotel
    }

    fun getFood(): String? {
        return food
    }

    fun setFood(food: String?) {
        this.food = food
    }

    fun getTransport(): String? {
        return transport
    }

    fun setTransport(transport: String?) {
        this.transport = transport
    }
}