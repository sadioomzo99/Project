using JetBrains.Annotations;
using System;
using System.Collections;
using System.Collections.Generic;
using TMPro;
using Unity.Mathematics;
using Unity.VisualScripting;
using UnityEditor.Tilemaps;
using UnityEngine;



public class Player : MonoBehaviour
{
    [Range(1f, 10f)]
    public float moveSpeed = 3f;
    [Range(1, 30)]
    public float jumpFroce = 20f;
    float moveHorizontal;
    BoxCollider2D boxCollider2D;
    CapsuleCollider2D capsule;
    [SerializeField] private LayerMask layerMask;
    private Animator m_animator;
    protected bool m_combatIdle = false;
    protected bool notTakingHit = true;
    protected bool m_notDead = true;
    private bool facingRight = true;
    private bool isFalling = false;
    protected bool canMove=true;
    [SerializeField]private Canvas canvasObject;
    [SerializeField] private TextMeshProUGUI text;
    private int coin = 0;


    Rigidbody2D rgb2D;

    // Start is called before the first frame update
    void Start()
    {
        m_animator = GetComponent<Animator>();
        rgb2D = gameObject.GetComponent<Rigidbody2D>();
        boxCollider2D = GetComponent<BoxCollider2D>();
        capsule = GetComponent<CapsuleCollider2D>();


    }

    // Update is called once per frame
    void Update()
    {
        jump();
        PlayAinmation();
        Move();

    }
       
       
        

    
    private void FixedUpdate()
    {
        
     
        

    }

    private void OnTriggerEnter2D(Collider2D collision)
    {

        if (collision.CompareTag("Coin"))
        {
            Destroy(collision.gameObject);
            coin++;
            text.text = " "+coin;
        }
    }
    /* private void OnCollisionEnter2D(Collision2D collision)
     {
         if (collision.gameObject.CompareTag("Trap"))
         {
             Die();
         }
     }*/
    public void Move()
    {
       
        if (m_notDead && notTakingHit)
        {
            
            //// Handle input and movement
            moveHorizontal = Input.GetAxis("Horizontal");

            // Swap direction of sprite depending on walk direction
            if (moveHorizontal > 0 && !facingRight)
            {

                flip();
            }
            else if (moveHorizontal < 0 && facingRight)
            {

                flip();
            }

            rgb2D.velocity = new Vector2(moveHorizontal * moveSpeed, rgb2D.velocity.y);
        }
    }

    public void flip()
    {
        Vector3 currentScale = gameObject.transform.localScale;
        currentScale.x *= -1;
        gameObject.transform.localScale = currentScale;
        facingRight = !facingRight;
    }
    public void jump()
    {
        if (Input.GetButtonDown("Jump") && IsGrounded()&&m_notDead)
        {
            
            m_animator.SetTrigger("Jump");
            m_animator.SetBool("Grounded", IsGrounded());
            rgb2D.velocity = Vector2.up * jumpFroce;
            Debug.Log(rgb2D.velocity);
        }

        
    }

    
    private bool IsGrounded()
    {
       
        float extraHeightText = .01f;
        //for avoiding the situation when the character is next to a wall and the isGrounded function detects the Wall and let you jump again and again
        //RaycastHit2D raycastHit2 = Physics2D.BoxCast(boxCollider2D.bounds.center, boxCollider2D.bounds.size - new Vector3(0.1f, 0f, 0f), 0f, Vector2.down, extraHeightText, layerMask);
        //RaycastHit2D raycastHit2 = Physics2D.BoxCast(boxCollider2D.bounds.center, boxCollider2D.bounds.size , 0f, Vector2.down, extraHeightText, layerMask);
        RaycastHit2D raycastHit2= Physics2D.CapsuleCast(capsule.bounds.center, capsule.bounds.size,CapsuleDirection2D.Vertical, 0f,Vector2.down, extraHeightText,layerMask);
        Color color;
        if(raycastHit2.collider != null)
        {
            color = Color.green;
            isFalling = false;
        }
        else
        {
            color = Color.red;
            isFalling = true;
        }

       // Debug.DrawRay(boxCollider2D.bounds.center + new Vector3(boxCollider2D.bounds.extents.x, 0), Vector2.down * (boxCollider2D.bounds.extents.y + extraHeightText), color);
       // Debug.DrawRay(boxCollider2D.bounds.center - new Vector3(boxCollider2D.bounds.extents.x, 0), Vector2.down * (boxCollider2D.bounds.extents.y + extraHeightText), color);
       // Debug.DrawRay(boxCollider2D.bounds.center - new Vector3(boxCollider2D.bounds.extents.x, boxCollider2D.bounds.extents.y+ extraHeightText), Vector2.right * (boxCollider2D.bounds.extents.x), color);
       
        return raycastHit2.collider != null;
        
     
    }

    public void PlayerTakeDamge(float damage)
    {
        
        GameManager.gameManager.PlayerHealth.Damage(damage);
        if (GameManager.gameManager.PlayerHealth.CurrentHealth == 0||transform.localPosition.y< -33f) {
            Die();
            gameObject.SetActive(false);
            GameManager.gameManager.GameOver();
        }
        
         
        
    }

    public void PlayerHealing(float heal)
    {
        GameManager.gameManager.PlayerHealth.Healing(heal);
    }
    private void PlayAinmation()
    {
        
        //Check if character just landed on the ground
        if (IsGrounded())
        {

            m_animator.SetBool("Grounded", IsGrounded());
        }
        //Check if character just started falling
        if (!IsGrounded())
        {

            
            m_animator.SetBool("Grounded", IsGrounded());
        }
        //Set AirSpeed in animator
        m_animator.SetBool("Falling", isFalling);

        // m_animator.SetTrigger("falling");

        //Death
          
     
    
        //Hurt
         if (Input.GetKeyDown("q") && GameManager.gameManager.PlayerHealth.currentMaxHealth!=0)
         {
            notTakingHit = false;
                PlayerTakeDamge(1);
            m_animator.SetTrigger("Hurt");
            
           
            
            // Debug.Log(GameManager.gameManager.PlayerHealth.changeHealthBar());
                Debug.Log(GameManager.gameManager.PlayerHealth.CurrentHealth);
                Debug.Log(GameManager.gameManager.PlayerHealth.currentMaxHealth);
             
         }

        //Attack
        else if (Input.GetMouseButtonDown(0)&&m_notDead)
        {
            m_animator.SetTrigger("Attack");
        }

        //Change between idle and combat idle
       /* else if (Input.GetKeyDown("f"))
            m_combatIdle = !m_combatIdle;*/
        //Run

        if (Mathf.Abs(moveHorizontal) > Mathf.Epsilon)
        {
           
            m_animator.SetInteger("AnimState", 0);
           
        }
        /*//Combat Idle
        else if (m_combatIdle)
            m_animator.SetInteger("AnimState", 1);*/

        //Idle
        else
            m_animator.SetInteger("AnimState", 1);
        notTakingHit = true;
    }
    private IEnumerator WaitForAnimationToFininsh()
    {
        // Wait for the transition to end
        yield return new WaitUntil(() => m_animator.GetCurrentAnimatorStateInfo(0).normalizedTime <= 1.0f);

         // Do some action

         // Wait for the animation to end
         yield return new WaitWhile(() => m_animator.GetCurrentAnimatorStateInfo(0).normalizedTime <= 1.0f);

    }
    private IEnumerator DisaperingHealthBar()
    {

        
        yield return new WaitForSeconds(0.3f);
        canvasObject.enabled = false;

        

    }

    private void Die()
    {
                
             //stop player movement
        
            
            rgb2D.velocity = Vector2.zero;
        
            m_animator.SetTrigger("Death");
            m_notDead = !m_notDead;


            StartCoroutine(DisaperingHealthBar());
            



    }

    
}
