using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class TestPlayer : MonoBehaviour
{
   public TextMeshProUGUI text;
    public int coin;
    // Start is called before the first frame update
    private void OnTriggerEnter(Collider other)
    {
       
        if (other.gameObject.CompareTag("Coin")){
            Destroy(other.gameObject);
           text.text += coin;
        }
    }
}
