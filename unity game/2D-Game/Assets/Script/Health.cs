using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.WSA;

public class Health 
{
    //Fields
    public float currentHealth { get; set; }
    public float currentMaxHealth { get; set; }
    protected float currentFill;

    //Proprties
    public float CurrentHealth
    {
        get
        {
            return currentHealth;
        }
        set
        {
            
          currentHealth = value;
           
        }
    }
    public float MaxHealth
    {
        get
        {
            return currentMaxHealth;
        }
        set
        {

            currentMaxHealth = value;
            

        }
    }



    //Constructer
    public Health(float health, float maxHealth)
    {
        currentHealth = health;
        currentMaxHealth = maxHealth;
       
    }
    
    //Methods
    public void Damage(float damageAmount)
    {
        if (currentHealth > 0)
        {
            currentHealth -= damageAmount;
           
        }
    }

    public void Healing(float healAmount)
    {
        if (currentHealth < currentMaxHealth)
        {
            currentHealth += healAmount ;
        }
        if (currentHealth > currentMaxHealth)
        {
            currentHealth = currentMaxHealth;
        }
    }

    public float changeHealthBar()
    {
          return currentFill =currentHealth/currentMaxHealth;
    }
}
