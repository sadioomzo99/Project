using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class Healthbar : MonoBehaviour
{
    [SerializeField]
     Image content;
    [SerializeField]
    TextMeshProUGUI text;






    // Start is called before the first frame update
    void Awake()
    {

        content = transform.GetChild(1).GetComponent<Image>();
        text = transform.GetChild(2).GetComponentInChildren<TextMeshProUGUI>();
        

    }

    // Update is called once per frame
    void Update()
    {
        text.text = GameManager.gameManager.PlayerHealth.currentHealth.ToString();
        content.fillAmount = GameManager.gameManager.PlayerHealth.changeHealthBar();
    }
}
