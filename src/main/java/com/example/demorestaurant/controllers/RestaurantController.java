package com.example.demorestaurant.controllers;

import com.example.demorestaurant.controllers.dtos.response.BaseResponse;
import com.example.demorestaurant.services.interfaces.IRestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demorestaurant.controllers.dtos.request.CreateRestaurantRequest;
import com.example.demorestaurant.controllers.dtos.request.UpdateRestaurantRequest;
import com.example.demorestaurant.controllers.dtos.response.CreateRestaurantResponse;
import com.example.demorestaurant.controllers.dtos.response.GetRestaurantResponse;
import com.example.demorestaurant.controllers.dtos.response.UpdateRestaurantResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("restaurant")
public class RestaurantController {
    @Autowired
    private IRestaurantService service;

    @PostMapping
    public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request){
        return service.create(request);
    }

    @GetMapping("{id}")
    public GetRestaurantResponse get(@PathVariable Long id){
        return service.get(id);
    }

    @PutMapping("{id}")
    public UpdateRestaurantResponse update(@RequestBody UpdateRestaurantRequest request, @PathVariable Long id){
        return service.update(request, id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
    }

    @GetMapping("{restaurantId}")
    public ResponseEntity<BaseResponse> getRestaurantByRestaurantId(@PathVariable Long restaurantId){
        BaseResponse baseResponse = service.getRestaurantByRestaurantId(restaurantId);
        return new ResponseEntity<>(baseResponse, baseResponse.getHttpStatus());
    }

    @GetMapping("restaurants")
    public ResponseEntity<BaseResponse> listAllRestaurants(){
        BaseResponse baseResponse = service.listAllRestaurants();
        return new ResponseEntity<>(baseResponse, baseResponse.getHttpStatus());
    }
}
