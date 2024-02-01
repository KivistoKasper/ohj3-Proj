/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package fi.tuni.prog3.sisu;

import com.google.gson.JsonObject;

/**
 * Interface for extracting data from the Sisu API.
 */
public interface iAPI {
    /**
     * Returns a JsonObject that is extracted from the Sisu API.
     * @param urlString URL for retrieving information from the Sisu API.
     * @return JsonObject.
     */
    public JsonObject getJsonObjectFromApi(String urlString);
}
